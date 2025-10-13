package com.template.base.infrastructure.security.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: HttpStatus, val code: String, val defaultMessage: String) {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_REFRESH_TOKEN", "유효하지 않은 Refresh Token입니다."),

    INVALID_AUTH_HEADER(HttpStatus.BAD_REQUEST, "INVALID_AUTH_HEADER", "Authorization 헤더 형식이 올바르지 않습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "유효하지 않은 Access Token입니다."),

    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN", "Access Token이 만료되었습니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER_ALREADY_DELETED", "이미 비활성화된 사용자입니다."),
    USER_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, "USER_ALREADY_ACTIVE", "이미 활성화된 사용자입니다."),

    MISSING_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "MISSING_ACCESS_TOKEN", "Access Token이 누락되었습니다."),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR", "데이터베이스 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류 발생")
}