package com.template.base.presentation.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 로그인 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 값 제외
data class LoginResponse(
    @Schema(description = "JWT Access Token (인증 시 사용)", example = "eyJhbGciOiJIUzI1...")
    val accessToken: String? = null,

    @Schema(description = "JWT Refresh Token (새로운 Access Token 발급용)", example = "eyJhbGciOiJIUzI1...")
    val refreshToken: String? = null,

    @Schema(description = "로그인 실패 시 에러 메시지", example = "Invalid email or password")
    val error: String? = null
)