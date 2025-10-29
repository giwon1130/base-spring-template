package com.template.platform.common.sse

import com.template.platform.features.notification.domain.NotificationDto
import mu.KotlinLogging
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

@Component
class NotificationSubscriber(
    private val sseManager: SseManager
) : MessageListener {

    private val logger = KotlinLogging.logger {}

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val raw = message.body.toString(Charsets.UTF_8)
        logger.info { "Redis 알림 수신: $raw" }

        try {
            // 외부에서 파싱한 객체를 받아서 전송
            val notification = NotificationDto.fromJson(raw)
            sseManager.sendNotification(notification.userEmail, notification)
        } catch (e: Exception) {
            logger.error(e) { "알림 메시지 처리 중 오류 발생: $raw" }
        }
    }
}