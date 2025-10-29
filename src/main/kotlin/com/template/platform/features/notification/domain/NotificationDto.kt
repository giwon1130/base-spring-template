package com.template.platform.features.notification.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant

/**
 * BMOA와 호환되는 알림 DTO.
 *
 * 기존 시스템에서 사용하던 필드를 유지하면서 템플릿에서 추가로 필요할 수 있는
 * 제목/메시지/메타데이터 필드를 선택적으로 확장했다.
 */
data class NotificationDto(
    val id: String,
    val userEmail: String,
    val sceneId: Long? = null,
    val sceneName: String? = null,
    val status: NotificationStatus? = null,
    val description: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String? = null,
    val formattedDate: String = Instant.ofEpochMilli(timestamp).toString(),
    var isRead: Boolean = false,
    val title: String? = null,
    val message: String? = null,
    val type: NotificationType? = null,
    val metadata: Map<String, Any?> = emptyMap()
) {
    fun toJson(): String = jacksonObjectMapper().writeValueAsString(this)

    companion object {
        fun fromJson(json: String): NotificationDto =
            jacksonObjectMapper().readValue(json)
    }
}

/**
 * BMOA 알림 상태.
 */
enum class NotificationStatus {
    RECEIVED,
    COMPLETED,
    RECEIVE_ERROR,
    ANALYSIS_ERROR,
    UNKNOWN
}

/**
 * 템플릿 확장 알림 타입 (선택 사항).
 */
enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    CHANGESET,
    SYSTEM
}

/**
 * 알림 읽음 상태 필터.
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
