package com.template.platform.features.notification.application

import com.template.platform.common.sse.RedisNotificationPublisher
import com.template.platform.features.notification.domain.InferenceNotificationPayload
import com.template.platform.features.notification.domain.InferenceStatus
import com.template.platform.features.notification.domain.NotificationDto
import com.template.platform.features.notification.domain.NotificationReadStatus
import com.template.platform.features.notification.domain.NotificationStatus
import com.template.platform.features.notification.domain.SceneNotificationPayload
import com.template.platform.features.notification.domain.SceneStatus
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.math.ceil

/**
 * SSE 알림 조회/관리 + 발행 담당 서비스.
 *
 * BMOA의 구조와 API 호환성을 유지하면서 템플릿 프로젝트에서 재사용한다.
 */
@Service
class NotificationService(
    private val redisNotificationPublisher: RedisNotificationPublisher,
    private val redisTemplate: StringRedisTemplate,
    private val notificationTargetResolver: NotificationTargetResolver
) {

    private val logger = KotlinLogging.logger {}

    fun publishSceneNotification(scene: SceneNotificationPayload) {
        val (notificationStatus, description) = getSceneNotificationInfo(scene.status)
        val targets = notificationTargetResolver.resolveSceneTargets(scene)

        targets.forEach { userEmail ->
            val notification = NotificationDto(
                id = UUID.randomUUID().toString(),
                userEmail = userEmail,
                sceneId = scene.sceneId,
                sceneName = scene.name,
                status = notificationStatus,
                description = description,
                timestamp = System.currentTimeMillis(),
                location = scene.location()
            )
            redisNotificationPublisher.publish(notification)
        }
    }

    fun publishInferenceNotification(inference: InferenceNotificationPayload) {
        val (notificationStatus, description) = getInferenceNotificationInfo(inference.status)
        val targets = notificationTargetResolver.resolveInferenceTargets(inference)

        targets.forEach { userEmail ->
            val notification = NotificationDto(
                id = UUID.randomUUID().toString(),
                userEmail = userEmail,
                sceneId = inference.targetScene.sceneId,
                sceneName = inference.targetScene.name,
                status = notificationStatus,
                description = description,
                timestamp = System.currentTimeMillis(),
                location = inference.targetScene.location()
            )
            redisNotificationPublisher.publish(notification)
        }
    }

    fun getUnreadNotificationCount(userEmail: String): Long {
        return try {
            val keys = redisTemplate.keys("notification:$userEmail:*")
            keys.count { key ->
                val isRead = redisTemplate.opsForHash<String, String>().get(key, "isRead")
                isRead?.equals("true", ignoreCase = true) != true
            }.toLong()
        } catch (e: Exception) {
            logger.error(e) { "읽지 않은 알림 수 조회 실패 - userEmail=$userEmail" }
            0L
        }
    }

    fun getNotificationList(
        userEmail: String,
        status: String,
        pageable: Pageable
    ): PageResponse<NotificationDto> {
        return try {
            val redisKey = "notification:$userEmail"
            val totalElements = redisTemplate.opsForList().size(redisKey) ?: 0L

            if (totalElements == 0L) {
                return PageResponse.empty(pageable)
            }

            val start = pageable.offset
            val end = start + pageable.pageSize - 1

            val notifications = redisTemplate.opsForList()
                .range(redisKey, start, end)
                ?.mapNotNull { json ->
                    try {
                        val notification = NotificationDto.fromJson(json)
                        val hashKey = "notification:$userEmail:${notification.id}"
                        val isReadValue = redisTemplate
                            .opsForHash<String, String>()
                            .get(hashKey, "isRead")
                        if (isReadValue != null) {
                            notification.isRead = isReadValue.equals("true", ignoreCase = true)
                        }
                        notification
                    } catch (e: Exception) {
                        logger.warn(e) { "알림 JSON 파싱 실패: $json" }
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

            val totalPages = if (pageable.pageSize == 0) 0 else ceil(
                totalElements.toDouble() / pageable.pageSize
            ).toInt()

            PageResponse(
                content = notifications,
                totalElements = totalElements,
                totalPages = totalPages,
                page = pageable.pageNumber,
                size = pageable.pageSize,
                isFirst = pageable.pageNumber == 0,
                isLast = if (totalPages == 0) true else pageable.pageNumber >= totalPages - 1
            )
        } catch (e: Exception) {
            logger.error(e) { "알림 목록 조회 실패 - userEmail=$userEmail" }
            PageResponse.empty(pageable)
        }
    }

    fun markAllAsRead(userEmail: String) {
        try {
            val keys = redisTemplate.keys("notification:$userEmail:*")
            keys.forEach { key ->
                redisTemplate.opsForHash<String, String>().put(key, "isRead", "true")
            }
        } catch (e: Exception) {
            logger.error(e) { "알림 전체 읽음 처리 실패 - userEmail=$userEmail" }
        }
    }

    private fun getSceneNotificationInfo(status: SceneStatus): Pair<NotificationStatus, String> =
        when (status) {
            SceneStatus.COMPLETED -> NotificationStatus.RECEIVED to "영상 수신이 완료되었습니다."
            SceneStatus.FAILED -> NotificationStatus.RECEIVE_ERROR to "영상 수신 중 오류가 발생했습니다."
            SceneStatus.PENDING -> NotificationStatus.UNKNOWN to "현재 상태를 확인할 수 없습니다."
            SceneStatus.UNKNOWN -> NotificationStatus.UNKNOWN to "현재 상태를 확인할 수 없습니다."
        }

    private fun getInferenceNotificationInfo(status: InferenceStatus): Pair<NotificationStatus, String> =
        when (status) {
            InferenceStatus.COMPLETED -> NotificationStatus.COMPLETED to "분석이 완료되었습니다."
            InferenceStatus.FAILED -> NotificationStatus.ANALYSIS_ERROR to "분석 중 오류가 발생했습니다."
            InferenceStatus.IN_PROGRESS,
            InferenceStatus.PENDING -> NotificationStatus.UNKNOWN to "현재 상태를 확인할 수 없습니다."
            InferenceStatus.UNKNOWN -> NotificationStatus.UNKNOWN to "현재 상태를 확인할 수 없습니다."
        }
}

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val isFirst: Boolean,
    val isLast: Boolean
) {
    companion object {
        fun <T> empty(pageable: Pageable): PageResponse<T> = PageResponse(
            content = emptyList(),
            totalElements = 0,
            totalPages = 0,
            page = pageable.pageNumber,
            size = pageable.pageSize,
            isFirst = true,
            isLast = true
        )
    }
}
