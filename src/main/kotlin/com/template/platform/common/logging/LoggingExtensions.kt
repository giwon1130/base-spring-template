package com.template.platform.common.logging

import mu.KLogger

/**
 * KLogger 확장 함수들
 * 
 * 주요 기능:
 * - 구조화된 로깅 (성공, 시작, 완료, 에러, 경고, 디버그)
 * - 자동 민감정보 마스킹
 * - 성능 측정 로깅
 * - 사용자 액션 로깅
 * - API 호출 로깅
 */

/**
 * 성공 작업 로깅
 * 
 * @param action 작업명
 * @param details 추가 정보
 */
fun KLogger.logSuccess(
    action: String,
    details: Map<String, Any?> = emptyMap()
) {
    val message = LoggingUtils.createStructuredLog(action, "성공", details)
    this.info(message)
}

/**
 * 시작 작업 로깅
 * 
 * @param action 작업명
 * @param details 추가 정보
 */
fun KLogger.logStart(
    action: String,
    details: Map<String, Any?> = emptyMap()
) {
    val message = LoggingUtils.createStructuredLog(action, "시작", details)
    this.info(message)
}

/**
 * 완료 작업 로깅
 * 
 * @param action 작업명
 * @param details 추가 정보
 */
fun KLogger.logComplete(
    action: String,
    details: Map<String, Any?> = emptyMap()
) {
    val message = LoggingUtils.createStructuredLog(action, "완료", details)
    this.info(message)
}

/**
 * 에러 로깅 (민감정보 자동 마스킹)
 * 
 * @param action 수행하려던 작업
 * @param errorType 에러 타입
 * @param errorMessage 에러 메시지
 * @param details 추가 정보 (자동으로 민감정보 마스킹됨)
 * @param throwable 예외 객체 (선택)
 */
fun KLogger.logError(
    action: String,
    errorType: String,
    errorMessage: String,
    details: Map<String, Any?> = emptyMap(),
    throwable: Throwable? = null
) {
    val maskedDetails = details.mapValues { (key, value) ->
        when {
            key.contains("email", ignoreCase = true) -> 
                LoggingUtils.maskEmail(value?.toString())
            key.contains("userId", ignoreCase = true) -> 
                LoggingUtils.maskUserId(value?.toString()?.toLongOrNull())
            key.contains("name", ignoreCase = true) -> 
                LoggingUtils.maskUserName(value?.toString())
            key.contains("phone", ignoreCase = true) -> 
                LoggingUtils.maskPhoneNumber(value?.toString())
            key.contains("path", ignoreCase = true) -> 
                LoggingUtils.maskFilePath(value?.toString())
            else -> value
        }
    }
    
    val message = LoggingUtils.createErrorLog(action, errorType, errorMessage, maskedDetails)
    
    if (throwable != null) {
        this.error(message, throwable)
    } else {
        this.error(message)
    }
}

/**
 * 경고 로깅
 * 
 * @param action 작업명
 * @param warningMessage 경고 메시지
 * @param details 추가 정보
 */
fun KLogger.logWarning(
    action: String,
    warningMessage: String,
    details: Map<String, Any?> = emptyMap()
) {
    val message = LoggingUtils.createStructuredLog(action, "경고", details + ("warning" to warningMessage))
    this.warn(message)
}

/**
 * 디버그 로깅 (개발 환경에서만)
 * 
 * @param action 작업명
 * @param details 추가 정보
 */
fun KLogger.logDebug(
    action: String,
    details: Map<String, Any?> = emptyMap()
) {
    if (this.isDebugEnabled) {
        val message = LoggingUtils.createStructuredLog(action, "디버그", details)
        this.debug(message)
    }
}

/**
 * 성능 측정 로깅
 * 
 * @param action 작업명
 * @param durationMs 소요 시간 (밀리초)
 * @param details 추가 정보
 */
fun KLogger.logPerformance(
    action: String,
    durationMs: Long,
    details: Map<String, Any?> = emptyMap()
) {
    val perfDetails = details + ("duration" to LoggingUtils.formatDuration(durationMs))
    val message = LoggingUtils.createStructuredLog(action, "성능", perfDetails)
    this.info(message)
}

/**
 * 사용자 액션 로깅 (민감정보 자동 마스킹)
 * 
 * @param action 사용자가 수행한 작업
 * @param userId 사용자 ID (자동 마스킹됨)
 * @param userEmail 사용자 이메일 (자동 마스킹됨, 선택)
 * @param details 추가 정보
 */
fun KLogger.logUserAction(
    action: String,
    userId: Long?,
    userEmail: String? = null,
    details: Map<String, Any?> = emptyMap()
) {
    val userDetails = mutableMapOf<String, Any?>()
    
    userId?.let { userDetails["userId"] = LoggingUtils.maskUserId(it) }
    userEmail?.let { userDetails["email"] = LoggingUtils.maskEmail(it) }
    userDetails.putAll(details)
    
    val message = LoggingUtils.createStructuredLog(action, "사용자액션", userDetails)
    this.info(message)
}

/**
 * API 호출 로깅
 * 
 * @param method HTTP 메서드
 * @param endpoint API 엔드포인트
 * @param statusCode HTTP 상태 코드
 * @param durationMs 소요 시간 (밀리초)
 * @param userId 사용자 ID (선택, 자동 마스킹됨)
 */
fun KLogger.logApiCall(
    method: String,
    endpoint: String,
    statusCode: Int,
    durationMs: Long,
    userId: Long? = null
) {
    val details = mutableMapOf<String, Any?>(
        "method" to method,
        "endpoint" to endpoint,
        "status" to statusCode,
        "duration" to LoggingUtils.formatDuration(durationMs)
    )
    
    userId?.let { details["userId"] = LoggingUtils.maskUserId(it) }
    
    val message = LoggingUtils.createStructuredLog("API호출", "완료", details)
    this.info(message)
}

/**
 * 데이터베이스 작업 로깅
 * 
 * @param operation DB 작업 (CREATE, READ, UPDATE, DELETE)
 * @param table 테이블명
 * @param recordId 레코드 ID (선택)
 * @param durationMs 소요 시간 (선택)
 */
fun KLogger.logDatabaseOperation(
    operation: String,
    table: String,
    recordId: Any? = null,
    durationMs: Long? = null
) {
    val details = mutableMapOf<String, Any?>(
        "operation" to operation,
        "table" to table
    )
    
    recordId?.let { details["recordId"] = it }
    durationMs?.let { details["duration"] = LoggingUtils.formatDuration(it) }
    
    val message = LoggingUtils.createStructuredLog("DB작업", "완료", details)
    this.info(message)
}

/**
 * 외부 API 호출 로깅
 * 
 * @param serviceName 외부 서비스명
 * @param endpoint 호출 엔드포인트
 * @param statusCode 응답 상태 코드
 * @param durationMs 소요 시간
 * @param details 추가 정보
 */
fun KLogger.logExternalApiCall(
    serviceName: String,
    endpoint: String,
    statusCode: Int,
    durationMs: Long,
    details: Map<String, Any?> = emptyMap()
) {
    val apiDetails = details + mapOf(
        "service" to serviceName,
        "endpoint" to endpoint,
        "status" to statusCode,
        "duration" to LoggingUtils.formatDuration(durationMs)
    )
    
    val message = LoggingUtils.createStructuredLog("외부API호출", "완료", apiDetails)
    this.info(message)
}