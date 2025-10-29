package com.template.platform.features.user.presentation.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email(message = "올바른 이메일 형식을 입력해주세요.")
    @field:NotBlank(message = "이메일을 입력해주세요.")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    val password: String
)
