package com.template.platform.features.notification.domain

/**
 * Scene 알림 정보에서 필요한 최소 필드만 모아놓은 페이로드.
 * 실제 Scene 도메인이 이식되기 전까지는 이 VO를 통해 정보를 전달한다.
 */
data class SceneNotificationPayload(
    val sceneId: Long,
    val name: String,
    val status: SceneStatus,
    val province: String? = null,
    val district: String? = null
) {
    /**
     * 지역 정보를 합쳐 알림에 표시할 문자열을 만든다.
     */
    fun location(): String? {
        val parts = listOfNotNull(province?.trim(), district?.trim())
        return if (parts.isEmpty()) null else parts.joinToString(" ")
    }
}

/**
 * 인퍼런스 알림에 필요한 데이터 묶음.
 */
data class InferenceNotificationPayload(
    val inferenceId: Long,
    val status: InferenceStatus,
    val targetScene: SceneNotificationPayload
)

/**
 * Scene 상태.
 */
enum class SceneStatus {
    PENDING,
    COMPLETED,
    FAILED,
    UNKNOWN
}

/**
 * 인퍼런스 상태.
 */
enum class InferenceStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    UNKNOWN
}
