package com.template.base.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * 로그아웃 요청 DTO
 */
@Schema(description = "로그아웃 요청")
data class LogoutRequest(
    @Schema(description = "Refresh Token", example = "uuid-refresh-token")
    @field:NotBlank(message = "Refresh Token을 입력해주세요.")
    val refreshToken: String
)