package com.template.base.infrastructure.security.exception

class CustomException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.defaultMessage // 기본 메시지를 사용
) : RuntimeException(message)