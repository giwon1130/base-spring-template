package com.template.base.application.dto

/**
 * 사용자 정보 DTO (서비스 계층 내부 사용)
 */
data class UserDto(
    val email: String,
    val password: String
)