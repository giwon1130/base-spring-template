package com.template.platform.features.notification.presentation

import com.template.platform.common.response.CommonResponse
import com.template.platform.common.sse.RedisNotificationPublisher
import com.template.platform.common.sse.SseManager
import com.template.platform.features.notification.application.NotificationService
import com.template.platform.features.notification.application.PageResponse
import com.template.platform.features.notification.domain.NotificationDto
import com.template.platform.features.notification.domain.NotificationStatus
import com.template.platform.features.notification.domain.NotificationType
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

/**
 * SSE 알림 컨트롤러 (BMOA 호환)
 */
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val sseManager: SseManager,
    private val redisNotificationPublisher: RedisNotificationPublisher,
    private val notificationService: NotificationService
) {
    private val logger = KotlinLogging.logger {}
    
    /**
     * SSE 구독 요청
     */
    @GetMapping("/stream/{userEmail}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamNotifications(@PathVariable userEmail: String): SseEmitter {
        logger.info { "사용자 '$userEmail'와 SSE 연결을 시도합니다." }
        return sseManager.createEmitter(userEmail)
    }
    /**
     * 알림 읽음 처리
     */
    @PostMapping("/{notificationId}/read")
    fun markAsRead(
        @RequestParam userEmail: String,
        @PathVariable notificationId: String
    ): CommonResponse<String> {
        redisNotificationPublisher.markAsRead(userEmail, notificationId)
        return CommonResponse.success(data = "Notification marked as read.")
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/{userEmail}/unread-count")
    fun getUnreadCount(@PathVariable userEmail: String): CommonResponse<Long> {
        val count = notificationService.getUnreadNotificationCount(userEmail)
        return CommonResponse.success(data = count)
    }

    /**
     * 전체 알림 조회
     */
    @GetMapping("/{userEmail}/list")
    fun getNotificationList(
        @PathVariable userEmail: String,
        @RequestParam status: String = "ALL",
        pageable: Pageable
    ): CommonResponse<PageResponse<NotificationDto>> {
        val notifications = notificationService.getNotificationList(userEmail, status, pageable)
        return CommonResponse.success(data = notifications)
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PostMapping("/{userEmail}/read-all")
    fun markAllAsRead(@PathVariable userEmail: String): CommonResponse<String> {
        notificationService.markAllAsRead(userEmail)
        return CommonResponse.success(data = "All notifications marked as read.")
    }

    /**
     * 테스트용 알림 전송
     */
    @PostMapping("/test/send")
    fun sendTestNotification(
        @RequestParam userEmail: String,
        @RequestParam message: String,
        @RequestParam(defaultValue = "INFO") type: String
    ): CommonResponse<String> {
        val notification = NotificationDto(
            id = UUID.randomUUID().toString(),
            userEmail = userEmail,
            status = NotificationStatus.UNKNOWN,
            description = message,
            title = "테스트 알림",
            message = message,
            type = NotificationType.valueOf(type)
        )

        redisNotificationPublisher.publish(notification)
        return CommonResponse.success(data = "Test notification sent to user: $userEmail")
    }
    
    /**
     * 연결 상태 조회
     */
    @GetMapping("/status")
    fun getStatus(): CommonResponse<Map<String, Any>> {
        val status = mapOf(
            "connectionCount" to sseManager.getConnectionCount(),
            "connectedUsers" to sseManager.getConnectedUsers()
        )
        return CommonResponse.success(data = status)
    }
}
