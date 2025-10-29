package com.template.platform.common.response

import com.template.platform.common.error.ErrorCode
import java.time.Instant

/**
 * Template 표준 응답 형식
 */
data class ApiResponse<T>(
    val status: String = "SUCCESS",
    val message: String = "요청이 정상 처리되었습니다.",
    val data: T?
) {
    companion object {
        fun <T> success(message: String = "요청이 정상 처리되었습니다.", data: T? = null): ApiResponse<T> {
            return ApiResponse(status = "SUCCESS", message = message, data = data)
        }
    }
}

/**
 * BMOA 표준 응답 형식 (하위 호환성)
 */
data class CommonResponse<T>(
    val status: String = "SUCCESS",
    val message: String = "요청이 정상 처리되었습니다.",
    val data: T?
) {
    companion object {
        fun <T> success(message: String = "요청이 정상 처리되었습니다.", data: T? = null): CommonResponse<T> {
            return CommonResponse(status = "SUCCESS", message = message, data = data)
        }
    }
}

/**
 * BMOA 에러 응답 형식
 */
data class ErrorResponse(
    val errorCode: String,
    val message: String?,
    val traceId: String?,
    val path: String?,
    val timestamp: String
) {
    companion object {
        fun from(
            errorCode: ErrorCode,
            message: String?,
            traceId: String?,
            path: String?
        ): ErrorResponse {
            return ErrorResponse(
                errorCode = errorCode.code,
                message = message ?: errorCode.defaultMessage,
                traceId = traceId,
                path = path,
                timestamp = Instant.now().toString()
            )
        }
    }
}
