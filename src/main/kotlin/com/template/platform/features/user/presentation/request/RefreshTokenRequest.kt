package com.template.platform.features.user.presentation.request

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "리프레시 토큰은 필수 입력 값입니다.")
    val refreshToken: String
)
