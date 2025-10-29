package com.template.platform.common.kafka

import com.template.platform.common.error.ErrorCode
import com.template.platform.common.error.PlatformException
import mu.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Kafka 메시지 처리 멱등성 보장 가드
 * 
 * Redis SETNX + TTL을 사용한 중복 처리 방지
 */
@Component
class IdempotencyGuard(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = KotlinLogging.logger {}
    
    companion object {
        private const val KEY_PREFIX = "platform:idempotency:"
        private val DEFAULT_TTL = Duration.ofHours(24) // 24시간
    }
    
    /**
     * 멱등성 키 획득 시도
     * 
     * @param messageId 메시지 고유 ID
     * @param ttl 키 유효 시간 (기본 24시간)
     * @return true: 처리 가능, false: 이미 처리됨 (중복)
     */
    fun tryAcquire(messageId: String, ttl: Duration = DEFAULT_TTL): Boolean {
        val key = KEY_PREFIX + messageId
        
        return try {
            val acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, System.currentTimeMillis().toString(), ttl)
                ?: false
                
            if (acquired) {
                logger.debug { "Idempotency key acquired: $messageId" }
            } else {
                logger.warn { "Duplicate message detected: $messageId" }
            }
            
            acquired
        } catch (e: Exception) {
            logger.error(e) { "Failed to acquire idempotency key: $messageId" }
            // Redis 장애 시 처리 허용 (일시적 중복 허용)
            true
        }
    }
    
    /**
     * 멱등성 키 강제 해제
     * 
     * 처리 실패 시 재처리를 위해 사용
     */
    fun release(messageId: String) {
        val key = KEY_PREFIX + messageId
        
        try {
            val deleted = redisTemplate.delete(key)
            if (deleted) {
                logger.info { "Idempotency key released: $messageId" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to release idempotency key: $messageId" }
        }
    }
    
    /**
     * 멱등성 키 존재 확인
     */
    fun exists(messageId: String): Boolean {
        val key = KEY_PREFIX + messageId
        return try {
            redisTemplate.hasKey(key)
        } catch (e: Exception) {
            logger.error(e) { "Failed to check idempotency key: $messageId" }
            false
        }
    }
    
    /**
     * 멱등성 검사와 함께 처리 실행
     * 
     * 중복 메시지인 경우 예외 발생
     */
    fun <T> executeOnce(messageId: String, ttl: Duration = DEFAULT_TTL, action: () -> T): T {
        if (!tryAcquire(messageId, ttl)) {
            throw PlatformException(ErrorCode.IDEMPOTENCY_CONFLICT)
        }
        
        return try {
            action()
        } catch (e: Exception) {
            // 처리 실패 시 키 해제하여 재시도 허용
            release(messageId)
            throw e
        }
    }
    
    /**
     * 멱등성 키 정보 조회
     */
    fun getKeyInfo(messageId: String): IdempotencyKeyInfo? {
        val key = KEY_PREFIX + messageId
        
        return try {
            val value = redisTemplate.opsForValue().get(key)
            val ttl = redisTemplate.getExpire(key)
            
            if (value != null) {
                IdempotencyKeyInfo(
                    messageId = messageId,
                    createdAt = value.toLongOrNull() ?: 0L,
                    ttlSeconds = ttl
                )
            } else null
        } catch (e: Exception) {
            logger.error(e) { "Failed to get idempotency key info: $messageId" }
            null
        }
    }
}

/**
 * 멱등성 키 정보
 */
data class IdempotencyKeyInfo(
    val messageId: String,
    val createdAt: Long,
    val ttlSeconds: Long
)