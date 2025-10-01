package com.template.base.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @field:Email(message = "올바른 이메일 형식을 입력해주세요.")
    @field:NotBlank(message = "이메일을 입력해주세요.")
    @Schema(description = "사용자의 이메일 주소", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    @Schema(description = "사용자의 비밀번호", example = "password123")
    val password: String
)