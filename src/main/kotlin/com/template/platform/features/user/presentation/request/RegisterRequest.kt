package com.template.platform.features.user.presentation.request

import com.template.platform.features.user.domain.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "이메일을 입력해주세요.")
    @field:Email(message = "올바른 이메일 형식을 입력해주세요.")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    val password: String,

    @field:NotBlank(message = "이름을 입력해주세요.")
    val name: String,

    @field:NotNull(message = "사용자 역할을 선택해주세요.")
    val role: Role
)
