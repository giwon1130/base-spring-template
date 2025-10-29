package com.template.platform.common.error

import org.springframework.http.HttpStatus

/**
 * BMOA 표준 에러 코드 정의
 * 
 * 기존 BMOA 프로젝트와 호환성 유지
 */
enum class ErrorCode(val httpStatus: HttpStatus, val code: String, val defaultMessage: String) {
    // 4xx Client Errors
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_REFRESH_TOKEN", "유효하지 않은 Refresh Token입니다."),
    INVALID_AUTH_HEADER(HttpStatus.BAD_REQUEST, "INVALID_AUTH_HEADER", "Authorization 헤더 형식이 올바르지 않습니다."),
    
    // 401 Unauthorized
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "유효하지 않은 Access Token입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN", "Access Token이 만료되었습니다."),
    MISSING_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "MISSING_ACCESS_TOKEN", "Access Token이 누락되었습니다."),
    
    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    INVALID_SIGNATURE(HttpStatus.FORBIDDEN, "INVALID_SIGNATURE", "다운로드 링크가 유효하지 않거나 만료되었습니다."),
    
    // 404 Not Found
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "요청한 보고서를 찾을 수 없습니다."),
    
    // 202 Accepted (비동기 처리 중)
    REPORT_IN_PROGRESS(HttpStatus.ACCEPTED, "REPORT_IN_PROGRESS", "보고서가 생성 중입니다."),
    
    // 5xx Server Errors
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류 발생"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR", "데이터베이스 오류가 발생했습니다."),
    REPORT_FILE_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_FILE_MISSING", "보고서 파일이 서버에서 찾을 수 없습니다."),
    
    // 503 Service Unavailable
    REDIS_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "REDIS_CONNECTION_FAILED", "Redis 서버에 연결할 수 없습니다."),
    
    // STAC 관련
    STAC_RESPONSE_EMPTY(HttpStatus.BAD_REQUEST, "STAC_RESPONSE_EMPTY", "STAC 응답이 비어 있습니다."),
    UNSUPPORTED_COLLECTION(HttpStatus.BAD_REQUEST, "UNSUPPORTED_COLLECTION", "지원하지 않는 collection입니다."),
    
    // Template 추가 에러 코드들
    CHANGESET_NOT_FOUND(HttpStatus.NOT_FOUND, "CHANGESET_NOT_FOUND", "변경사항을 찾을 수 없습니다."),
    CHANGESET_INVALID_BBOX(HttpStatus.BAD_REQUEST, "CHANGESET_INVALID_BBOX", "유효하지 않은 BBOX 좌표입니다."),
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_SEND_FAILED", "알림 전송에 실패했습니다."),
    CACHE_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CACHE_OPERATION_FAILED", "캐시 작업에 실패했습니다."),
    KAFKA_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KAFKA_PUBLISH_FAILED", "메시지 발행에 실패했습니다."),
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", "중복 처리가 감지되었습니다.")
}
