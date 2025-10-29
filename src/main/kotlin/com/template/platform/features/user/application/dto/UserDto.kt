package com.template.platform.features.user.application.dto

/**
 * 로그인 처리 시 내부에서 사용하는 사용자 DTO.
 */
data class UserDto(
    val email: String,
    val password: String
)
