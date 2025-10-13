package com.template.base.presentation.dto.common

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 페이지네이션 응답 DTO
 */
@Schema(description = "페이지네이션 응답")
data class PageResponse<T>(
    @Schema(description = "현재 페이지 데이터")
    val content: List<T>,

    @Schema(description = "페이지 정보")
    val page: PageInfo
) {
    @Schema(description = "페이지 정보")
    data class PageInfo(
        @Schema(description = "현재 페이지 번호", example = "0")
        val number: Int,

        @Schema(description = "페이지 크기", example = "20")
        val size: Int,

        @Schema(description = "총 요소 수", example = "100")
        val totalElements: Long,

        @Schema(description = "총 페이지 수", example = "5")
        val totalPages: Int,

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        val first: Boolean,

        @Schema(description = "마지막 페이지 여부", example = "false")
        val last: Boolean,

        @Schema(description = "빈 페이지 여부", example = "false")
        val empty: Boolean
    )
}