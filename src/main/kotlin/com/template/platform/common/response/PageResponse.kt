package com.template.platform.common.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 공통 페이지 응답 DTO 
 *
 * @param content 조회된 데이터 리스트
 * @param totalElements 전체 데이터 개수
 * @param totalPages 전체 페이지 수
 * @param page 현재 페이지 번호 (0부터 시작)
 * @param size 페이지당 항목 수
 * @param isFirst 첫 페이지 여부
 * @param isLast 마지막 페이지 여부
 */
data class PageResponse<T>(
    @Schema(description = "조회된 데이터 리스트")
    val content: List<T>,

    @Schema(description = "전체 데이터 개수", example = "200")
    val totalElements: Long,

    @Schema(description = "전체 페이지 수", example = "20")
    val totalPages: Int,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    val page: Int,

    @Schema(description = "페이지당 항목 수", example = "10")
    val size: Int,

    @Schema(description = "첫 페이지 여부", example = "true")
    val isFirst: Boolean,

    @Schema(description = "마지막 페이지 여부", example = "false")
    val isLast: Boolean
)