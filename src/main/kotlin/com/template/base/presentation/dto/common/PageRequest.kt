package com.template.base.presentation.dto.common

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * 페이지네이션 요청 DTO
 */
@Schema(description = "페이지네이션 요청")
data class PageRequest(
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    @field:Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    val page: Int = 0,

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    @field:Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @field:Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    val size: Int = 20,

    @Schema(description = "정렬 기준", example = "createdAt,desc")
    val sort: String? = null
)