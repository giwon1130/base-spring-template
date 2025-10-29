package com.template.platform.features.notification.application

import com.template.platform.features.notification.domain.InferenceNotificationPayload
import com.template.platform.features.notification.domain.SceneNotificationPayload

/**
 * 알림을 어떤 사용자에게 보내야 하는지 결정하는 전략 인터페이스.
 *
 * BMOA에서는 역할/관심지역 등에 따라 다양한 구현체를 사용하므로
 * 동일한 구조를 유지한다.
 */
interface NotificationTargetResolver {

    /**
     * Scene 관련 이벤트 알림 타겟 목록.
     */
    fun resolveSceneTargets(scene: SceneNotificationPayload): List<String>

    /**
     * 인퍼런스 관련 알림 타겟 목록.
     */
    fun resolveInferenceTargets(inference: InferenceNotificationPayload): List<String>
}
