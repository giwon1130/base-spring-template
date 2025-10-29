package com.template.platform.common.sse

import com.template.platform.features.notification.domain.NotificationDto
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import jakarta.annotation.PreDestroy

/**
 * SSE 연결 관리자 (BMOA 호환)
 */
@Service
class SseManager {
    private val logger = KotlinLogging.logger {}
    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val keepAliveScheduler = Executors.newScheduledThreadPool(2)
    private val keepAliveTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()
    fun createEmitter(userId: String, timeout: Long = 0L): SseEmitter {
        val emitter = SseEmitter(timeout)
        
        // 기존 연결이 있다면 정리
        cleanupExistingConnection(userId)
        
        emitters[userId] = emitter

        emitter.onCompletion {
            cleanupConnection(userId)
            logger.info { "SSE 연결 종료 (완료): $userId" }
        }
        emitter.onTimeout {
            cleanupConnection(userId)
            logger.info { "SSE 연결 종료 (타임아웃): $userId" }
        }
        emitter.onError { throwable ->
            cleanupConnection(userId)
            logger.warn(throwable) { "SSE 연결 종료 (오류): $userId" }
        }

        // keep-alive 작업 스케줄링 (30초마다 전송)
        val keepAliveTask = keepAliveScheduler.scheduleAtFixedRate({
            emitters[userId]?.let { activeEmitter ->
                try {
                    activeEmitter.send(SseEmitter.event().comment("keep-alive"))
                } catch (e: Exception) {
                    cleanupConnection(userId)
                    logger.warn(e) { "keep-alive 전송 중 오류 발생 (연결 종료): $userId" }
                }
            }
        }, 0, 30, TimeUnit.SECONDS)
        
        keepAliveTasks[userId] = keepAliveTask

        logger.info { "SSE Emitter 생성 완료 (User: $userId, Timeout: $timeout ms)" }
        return emitter
    }
    
    private fun cleanupExistingConnection(userId: String) {
        emitters.remove(userId)
        keepAliveTasks.remove(userId)?.cancel(false)
    }
    
    private fun cleanupConnection(userId: String) {
        emitters.remove(userId)
        keepAliveTasks.remove(userId)?.cancel(false)
    }
    fun sendNotification(userId: String, notification: NotificationDto) {
        emitters[userId]?.let { emitter ->
            try {
                emitter.send(SseEmitter.event().data(notification))
                logger.debug { "SSE 알림 전송 성공: $userId" }
            } catch (e: Exception) {
                logger.warn(e) { "SSE 메시지 전송 실패: $userId" }
                cleanupConnection(userId)
            }
        } ?: run {
            logger.debug { "SSE Emitter를 찾을 수 없음: $userId" }
        }
    }
    
    /**
     * 현재 연결된 사용자 수 반환
     */
    fun getConnectedUserCount(): Int = emitters.size
    
    /**
     * 특정 사용자의 연결 상태 확인
     */
    fun isConnected(userId: String): Boolean = emitters.containsKey(userId)
    
    /**
     * 연결된 사용자 목록 반환 (호환성)
     */
    fun getConnectionCount(): Int = emitters.size
    fun getConnectedUsers(): List<String> = emitters.keys.toList()
    
    /**
     * 애플리케이션 종료 시 모든 리소스 정리
     */
    @PreDestroy
    fun shutdown() {
        logger.info { "SseManager 종료 - 연결된 사용자 수: ${emitters.size}" }
        
        // 모든 keep-alive 작업 취소
        keepAliveTasks.values.forEach { it.cancel(false) }
        keepAliveTasks.clear()
        
        // 모든 SSE 연결 종료
        emitters.values.forEach { emitter ->
            try {
                emitter.complete()
            } catch (e: Exception) {
                logger.warn(e) { "SSE 연결 종료 중 오류 발생" }
            }
        }
        emitters.clear()
        
        // 스케줄러 종료
        keepAliveScheduler.shutdown()
        try {
            if (!keepAliveScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                keepAliveScheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            keepAliveScheduler.shutdownNow()
            Thread.currentThread().interrupt()
        }
        
        logger.info { "SseManager 정리 완료" }
    }
}