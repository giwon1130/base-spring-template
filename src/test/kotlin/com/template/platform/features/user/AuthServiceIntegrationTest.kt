package com.template.platform.features.user

import com.template.platform.features.user.application.AuthService
import com.template.platform.features.user.domain.Role
import com.template.platform.features.user.domain.UserRepository
import com.template.platform.features.user.presentation.request.RegisterRequest
import com.template.platform.support.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class AuthServiceIntegrationTest @Autowired constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : IntegrationTestSupport() {

    @Test
    fun `회원가입을 수행하면 사용자 정보가 저장된다`() {
        val request = RegisterRequest(
            email = "tester-${UUID.randomUUID()}@example.com",
            password = "password123!",
            name = "통합테스트",
            role = Role.USER
        )

        val savedUser = authService.registerUser(request)

        val found = userRepository.findById(savedUser.userId)
        assertThat(found).isPresent
        assertThat(found.get().email).isEqualTo(request.email)
        assertThat(found.get().name).isEqualTo(request.name)
        assertThat(found.get().password).isNotEqualTo(request.password) // 암호화 확인
    }
}
