package com.template.base.presentation.dto.response

import com.template.base.infrastructure.security.exception.ErrorCode
import java.time.Instant

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