package com.template.platform.features.notification.application

import com.template.platform.features.notification.domain.NotificationDto
import com.template.platform.features.notification.domain.NotificationReadStatus
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

/**
 * 알림 서비스 (BMOA 호환)
 */
@Service
class NotificationService(
    private val redisTemplate: StringRedisTemplate
) {
    private val logger = KotlinLogging.logger {}
    /**
     * 읽지 않은 알림 개수 조회
     */
    fun getUnreadNotificationCount(userEmail: String): Long {
        return try {
            val listKey = "notification:$userEmail"
            val notifications = redisTemplate.opsForList().range(listKey, 0, -1) ?: emptyList()
            
            notifications.count { json ->
                try {
                    val notification = NotificationDto.fromJson(json)
                    !notification.isRead
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to parse notification JSON: $json" }
                    false
                }
            }.toLong()
        } catch (e: Exception) {
            logger.error(e) { "Error getting unread notification count for user: $userEmail" }
            0L
        }
    }
    
    /**
     * 알림 목록 조회 (페이징)
     */
    fun getNotificationList(
        userEmail: String, 
        status: String, 
        pageable: org.springframework.data.domain.Pageable
    ): PageResponse<NotificationDto> {
        return try {
            val listKey = "notification:$userEmail"
            val totalElements = redisTemplate.opsForList().size(listKey) ?: 0L
            
            val start = pageable.offset
            val end = start + pageable.pageSize - 1
            
            val notifications = redisTemplate.opsForList().range(listKey, start, end)
                ?.mapNotNull { json ->
                    try {
                        NotificationDto.fromJson(json)
                    } catch (e: Exception) {
                        logger.warn(e) { "Failed to parse notification JSON: $json" }
                        null
                    }
                }
                ?.filter { notification ->
                    when (NotificationReadStatus.from(status)) {
                        NotificationReadStatus.READ -> notification.isRead
                        NotificationReadStatus.UNREAD -> !notification.isRead
                        NotificationReadStatus.ALL -> true
                    }
                } ?: emptyList()
            
            PageResponse(
                content = notifications,
                totalElements = totalElements,
                totalPages = (totalElements + pageable.pageSize - 1) / pageable.pageSize,
                pageNumber = pageable.pageNumber,
                pageSize = pageable.pageSize,
                isFirst = pageable.pageNumber == 0,
                isLast = pageable.pageNumber >= (totalElements / pageable.pageSize)
            )
        } catch (e: Exception) {
            logger.error(e) { "Error getting notification list for user: $userEmail" }
            PageResponse.empty(pageable)
        }
    }
    
    /**
     * 모든 알림 읽음 처리
     */
    fun markAllAsRead(userEmail: String) {
        try {
            val listKey = "notification:$userEmail"
            val notifications = redisTemplate.opsForList().range(listKey, 0, -1) ?: emptyList()
            
            // 모든 알림을 읽음 상태로 변경
            notifications.forEach { json ->
                try {
                    val notification = NotificationDto.fromJson(json).copy(isRead = true)
                    val key = "notification:$userEmail:${notification.id}"
                    
                    // 개별 알림 상태 업데이트
                    redisTemplate.opsForHash<String, Any>().put(key, "data", notification.toJson())
                    redisTemplate.opsForHash<String, Any>().put(key, "isRead", "true")
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to mark notification as read: $json" }
                }
            }
            
            // 리스트도 업데이트 (읽음 상태로 변경된 알림들로 교체)
            redisTemplate.delete(listKey)
            notifications.forEach { json ->
                try {
                    val notification = NotificationDto.fromJson(json).copy(isRead = true)
                    redisTemplate.opsForList().leftPush(listKey, notification.toJson())
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to update notification in list: $json" }
                }
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Error marking all notifications as read for user: $userEmail" }
        }
    }
}

/**
 * 페이지 응답 DTO
 */
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Long,
    val pageNumber: Int,
    val pageSize: Int,
    val isFirst: Boolean,
    val isLast: Boolean
) {
    companion object {
        fun <T> empty(pageable: org.springframework.data.domain.Pageable): PageResponse<T> {
            return PageResponse(
                content = emptyList(),
                totalElements = 0L,
                totalPages = 0L,
                pageNumber = pageable.pageNumber,
                pageSize = pageable.pageSize,
                isFirst = true,
                isLast = true
            )
        }
    }
}