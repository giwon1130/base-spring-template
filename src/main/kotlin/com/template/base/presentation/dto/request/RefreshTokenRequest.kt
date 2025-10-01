package com.template.base.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * 리프레시 토큰 요청 DTO
 */
data class RefreshTokenRequest(
    @Schema(description = "새로운 Access Token을 발급받기 위한 Refresh Token", example = "eyJhbGciOiJIUzI1...")
    @field:NotBlank(message = "리프레시 토큰은 필수 입력 값입니다.")
    val refreshToken: String
)