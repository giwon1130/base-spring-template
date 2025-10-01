package com.template.base.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import com.template.base.domain.model.Role

/**
 * 회원가입 요청 DTO
 */
data class RegisterRequest(
    @Schema(description = "사용자의 이메일 주소", example = "user@example.com")
    @field:NotBlank(message = "이메일을 입력해주세요.")
    @field:Email(message = "올바른 이메일 형식을 입력해주세요.")
    val email: String,

    @Schema(description = "사용자의 비밀번호 (8자 이상)", example = "password123")
    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    val password: String,

    @Schema(description = "사용자의 이름", example = "홍길동")
    @field:NotBlank(message = "이름을 입력해주세요.")
    val name: String,

    @Schema(description = "사용자의 역할 (USER 또는 ADMIN)", example = "USER")
    @field:NotNull(message = "사용자 역할을 선택해주세요.")
    val role: Role
)