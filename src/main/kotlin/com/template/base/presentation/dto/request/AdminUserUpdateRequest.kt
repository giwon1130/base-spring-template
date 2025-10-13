package com.template.base.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import com.template.base.domain.model.Role

/**
 * 관리자용 사용자 정보 수정 요청 DTO
 */
@Schema(description = "관리자용 사용자 정보 수정 요청")
data class AdminUserUpdateRequest(
    @Schema(description = "사용자 이름", example = "홍길동")
    @field:NotBlank(message = "이름을 입력해주세요.")
    val name: String? = null,

    @Schema(description = "사용자 역할", example = "USER")
    val role: Role? = null,

    @Schema(description = "계정 활성화 상태", example = "true")
    val active: Boolean? = null
)