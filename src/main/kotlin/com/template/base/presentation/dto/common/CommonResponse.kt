package com.template.base.presentation.dto.common

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * API 응답의 표준 형식을 정의하는 클래스
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CommonResponse<T>(
    val status: String = "SUCCESS",
    val message: String = "요청이 정상 처리되었습니다.",
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T): CommonResponse<T> {
            return CommonResponse(data = data)
        }

        fun <T> success(message: String, data: T): CommonResponse<T> {
            return CommonResponse(message = message, data = data)
        }

        fun error(message: String): CommonResponse<Nothing> {
            return CommonResponse(status = "ERROR", message = message)
        }
    }
}