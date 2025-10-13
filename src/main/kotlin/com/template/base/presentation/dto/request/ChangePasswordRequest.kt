package com.template.base.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 비밀번호 변경 요청 DTO
 */
@Schema(description = "비밀번호 변경 요청")
data class ChangePasswordRequest(
    @Schema(description = "현재 비밀번호", example = "oldpassword123")
    @field:NotBlank(message = "현재 비밀번호를 입력해주세요.")
    val currentPassword: String,

    @Schema(description = "새 비밀번호 (8자 이상)", example = "newpassword123")
    @field:NotBlank(message = "새 비밀번호를 입력해주세요.")
    @field:Size(min = 8, message = "새 비밀번호는 최소 8자 이상이어야 합니다.")
    val newPassword: String,

    @Schema(description = "새 비밀번호 확인", example = "newpassword123")
    @field:NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
    val confirmPassword: String
)