package com.template.platform.features.notification.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant

/**
 * 템플릿 알림 정보 DTO
 */
data class NotificationDto(
    val id: String,
    val userEmail: String,
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.INFO,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = Instant.ofEpochMilli(timestamp).toString(),
    var isRead: Boolean = false,
    val metadata: Map<String, Any> = emptyMap()
) {
    fun toJson(): String = jacksonObjectMapper().writeValueAsString(this)

    companion object {
        fun fromJson(json: String): NotificationDto =
            jacksonObjectMapper().readValue(json)
    }
}

/**
 * 알림 타입
 */
enum class NotificationType {
    INFO,           // 일반 정보
    SUCCESS,        // 성공 알림
    WARNING,        // 경고
    ERROR,          // 오류
    CHANGESET,      // 변경사항 감지
    SYSTEM          // 시스템 알림
}

/**
 * 알림 읽음 상태 필터
 */
enum class NotificationReadStatus {
    ALL,
    READ,
    UNREAD;

    companion object {
        fun from(value: String): NotificationReadStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ALL
    }
}