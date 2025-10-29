package com.template.platform.features.notification.infrastructure

import com.template.platform.features.notification.application.NotificationTargetResolver
import com.template.platform.features.notification.domain.InferenceNotificationPayload
import com.template.platform.features.notification.domain.SceneNotificationPayload
import com.template.platform.features.user.domain.UserRepository
import org.springframework.stereotype.Component

/**
 * 현재 템플릿에서는 모든 활성 사용자에게 알림을 발송한다.
 * 향후 AOI/역할 기반 필터링 전략을 이 자리에 대체 구현하면 된다.
 */
@Component
class AllUserNotificationTargetResolver(
    private val userRepository: UserRepository
) : NotificationTargetResolver {

    override fun resolveSceneTargets(scene: SceneNotificationPayload): List<String> {
        return userRepository.findAll().map { it.email }
    }

    override fun resolveInferenceTargets(inference: InferenceNotificationPayload): List<String> {
        return userRepository.findAll().map { it.email }
    }
}
