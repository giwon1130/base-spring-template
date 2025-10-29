package com.template.platform.common.error

/**
 * 공통 플랫폼 예외 클래스.
 *
 * BMOA 프로젝트와의 호환을 위해 errorCode 기반 메시지를 유지한다.
 */
open class PlatformException(
    val errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: errorCode.defaultMessage, cause) {

    companion object {
        fun notFound(errorCode: ErrorCode) = PlatformException(errorCode)
        fun badRequest(errorCode: ErrorCode, message: String? = null) =
            PlatformException(errorCode, message)

        fun unauthorized(errorCode: ErrorCode) = PlatformException(errorCode)
        fun forbidden(errorCode: ErrorCode) = PlatformException(errorCode)
        fun internal(errorCode: ErrorCode, cause: Throwable? = null) =
            PlatformException(errorCode, null, cause)
    }
}

/**
 * BMOA 호환 CustomException.
 *
 * 기존 코드에서 CustomException을 그대로 사용할 수 있도록 PlatformException을 확장한다.
 */
class CustomException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : PlatformException(errorCode, message, cause)
