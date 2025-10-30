package com.template.platform.features.user.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun `User 엔티티 생성 테스트`() {
        // Given
        val email = "test@example.com"
        val password = "encodedPassword123"
        val name = "테스트 사용자"
        val role = Role.USER

        // When
        val user = User(
            email = email,
            password = password,
            name = name,
            role = role
        )

        // Then
        assertThat(user.email).isEqualTo(email)
        assertThat(user.password).isEqualTo(password)
        assertThat(user.name).isEqualTo(name)
        assertThat(user.role).isEqualTo(role)
        assertThat(user.userId).isEqualTo(0L) // 기본값
    }

    @Test
    fun `User 기본 Role은 USER이다`() {
        // When
        val user = User(
            email = "test@example.com",
            password = "password",
            name = "테스트"
        )

        // Then
        assertThat(user.role).isEqualTo(Role.USER)
    }

    @Test
    fun `User copy 함수 테스트`() {
        // Given
        val originalUser = User(
            userId = 1L,
            email = "original@example.com",
            password = "originalPassword",
            name = "원본 사용자",
            role = Role.USER
        )

        // When
        val copiedUser = originalUser.copy(
            name = "수정된 사용자",
            role = Role.ADMIN
        )

        // Then
        assertThat(copiedUser.userId).isEqualTo(originalUser.userId)
        assertThat(copiedUser.email).isEqualTo(originalUser.email)
        assertThat(copiedUser.password).isEqualTo(originalUser.password)
        assertThat(copiedUser.name).isEqualTo("수정된 사용자")
        assertThat(copiedUser.role).isEqualTo(Role.ADMIN)
    }

    @Test
    fun `User copy 함수로 일부 필드만 변경 테스트`() {
        // Given
        val originalUser = User(
            userId = 1L,
            email = "test@example.com",
            password = "password123",
            name = "테스트 사용자",
            role = Role.USER
        )

        // When
        val userWithNewPassword = originalUser.copy(password = "newPassword456")

        // Then
        assertThat(userWithNewPassword.userId).isEqualTo(originalUser.userId)
        assertThat(userWithNewPassword.email).isEqualTo(originalUser.email)
        assertThat(userWithNewPassword.name).isEqualTo(originalUser.name)
        assertThat(userWithNewPassword.role).isEqualTo(originalUser.role)
        assertThat(userWithNewPassword.password).isEqualTo("newPassword456")
    }

    @Test
    fun `Role enum 테스트`() {
        // When & Then
        assertThat(Role.USER).isNotNull
        assertThat(Role.ADMIN).isNotNull
        assertThat(Role.values()).hasSize(2)
        assertThat(Role.valueOf("USER")).isEqualTo(Role.USER)
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN)
    }
}