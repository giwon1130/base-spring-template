package com.template.base.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * 사용자 정보 수정 요청 DTO
 */
data class UpdateUserRequest(
    @Schema(description = "사용자의 이름", example = "홍길동")
    @field:NotBlank(message = "이름을 입력해주세요.")
    val name: String,
)