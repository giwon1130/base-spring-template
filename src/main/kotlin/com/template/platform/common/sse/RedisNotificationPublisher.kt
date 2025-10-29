package com.template.platform.common.sse

import com.template.platform.features.notification.domain.NotificationDto
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component

@Component
class RedisNotificationPublisher(
    private val redisTemplate: StringRedisTemplate,
    private val topic: ChannelTopic
) {

    /**
     * 알림 발행
     */
    fun publish(notification: NotificationDto) {
        val json = notification.toJson()
        redisTemplate.convertAndSend(topic.topic, json)
        saveNotificationToRedis(notification) // Redis에 알림 저장
    }

    /**
     * Redis에 알림 저장 (읽음/읽지 않음 상태 포함)
     */
    private fun saveNotificationToRedis(notification: NotificationDto) {
        val key = "notification:${notification.userEmail}:${notification.id}"
        val listKey = "notification:${notification.userEmail}"

        val json = notification.toJson()

        // 기존 Hash 저장 방식
        redisTemplate.opsForHash<String, Any>().put(key, "data", json)
        redisTemplate.opsForHash<String, Any>().put(key, "isRead", notification.isRead.toString())

        // 리스트에도 추가 (최신 알림이 앞에 오도록 leftPush)
        redisTemplate.opsForList().leftPush(listKey, json)
    }

    /**
     * 알림 읽음 상태 변경 (isRead = true)
     */
    fun markAsRead(userId: String, notificationId: String) {
        val key = "notification:$userId:$notificationId"
        val exists = redisTemplate.opsForHash<String, Any>().hasKey(key, "data")

        if (exists == true) {
            // Redis에서 알림 데이터 로드
            val json = redisTemplate.opsForHash<String, Any>().get(key, "data") as String
            val notification = NotificationDto.fromJson(json).copy(
                isRead = true // 읽음 상태 반영
            )

            // Redis에 읽음 상태 저장 (String으로 저장)
            redisTemplate.opsForHash<String, Any>().put(key, "data", notification.toJson())
            redisTemplate.opsForHash<String, Any>().put(key, "isRead", "true")

            // SSE로 읽음 상태 변경 전송
            redisTemplate.convertAndSend(topic.topic, notification.toJson())
        }
    }
}