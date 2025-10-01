package com.template.base.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import com.template.base.domain.model.Role

/**
 * 사용자 정보 응답 DTO
 */
data class UserResponse(
    @Schema(description = "사용자의 이메일 주소", example = "user@example.com")
    val email: String,

    @Schema(description = "사용자의 이름", example = "홍길동")
    val name: String,

    @Schema(description = "사용자의 역할 (USER 또는 ADMIN)", example = "USER")
    val role: Role,
)