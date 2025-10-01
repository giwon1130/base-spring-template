package com.template.base.infrastructure.security

import io.micrometer.tracing.Span
import io.micrometer.tracing.Tracer
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import com.template.base.infrastructure.security.exception.CustomException
import com.template.base.infrastructure.security.exception.ErrorCode
import com.template.base.presentation.dto.response.ErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler(private val tracer: Tracer) {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI

        logger.warn("사용자 없음: {}, traceId: {}", e.message, traceId)
        return createErrorResponse(ErrorCode.ENTITY_NOT_FOUND, e.message, traceId, path)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI

        logger.warn("잘못된 요청: {}, traceId: {}", e.message, traceId)
        return createErrorResponse(ErrorCode.INVALID_REQUEST, e.message, traceId, path)
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI

        logger.error("예외 발생: {}, 경로: {}, traceId: {}", ex.message, path, traceId)
        return createErrorResponse(
            errorCode = ex.errorCode,
            message = ex.message,
            traceId = traceId,
            path = path
        )
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDatabaseException(e: DataAccessException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI

        logger.error("데이터베이스 오류 발생, traceId: {}", traceId, e)
        return createErrorResponse(ErrorCode.DATABASE_ERROR, e.message, traceId, path)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI

        val errorMessages = ex.bindingResult.allErrors
            .map { error -> (error as FieldError).defaultMessage }
            .joinToString(", ")

        logger.error("유효성 검사 오류 발생: {}, traceId: {}", errorMessages, traceId)
        return createErrorResponse(ErrorCode.INVALID_REQUEST, errorMessages, traceId, path)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI

        logger.error("접근 거부: {}, traceId: {}", ex.message, traceId)
        return createErrorResponse(ErrorCode.FORBIDDEN, "접근 권한이 없습니다.", traceId, path)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("요청 JSON 파싱 오류: {}", e.message)
        val traceId = getTraceId()
        val path = request.requestURI
        return createErrorResponse(
            ErrorCode.INVALID_REQUEST,
            "요청 형식이 잘못되었습니다. 필수 값을 모두 입력했는지 확인해주세요.",
            traceId = traceId,
            path = path
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI
        val paramName = ex.name
        val value = ex.value
        val message = "요청 파라미터 '$paramName'에 잘못된 값이 들어왔습니다: '$value'"

        logger.warn("파라미터 타입 불일치: $message, traceId: $traceId")
        return createErrorResponse(ErrorCode.INVALID_REQUEST, message, traceId, path)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI
        val message = "필수 요청 파라미터 '${ex.parameterName}'가 누락되었습니다."

        logger.warn("필수 요청 파라미터 누락: $message, traceId: $traceId")
        return createErrorResponse(ErrorCode.INVALID_REQUEST, message, traceId, path)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val traceId = getTraceId()
        val path = request.requestURI

        logger.error("서버 오류 발생: {}, traceId: {}", e.message, traceId, e)
        return createErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR,
            e.message ?: ErrorCode.INTERNAL_SERVER_ERROR.defaultMessage,
            traceId,
            path
        )
    }

    private fun createErrorResponse(
        errorCode: ErrorCode,
        message: String?,
        traceId: String?,
        path: String?
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(errorCode.httpStatus).body(
            ErrorResponse.from(
                errorCode = errorCode,
                message = message,
                traceId = traceId,
                path = path
            )
        )
    }

    // Micrometer Tracer에서 TraceId 가져오기
    private fun getTraceId(): String? {
        val span: Span? = tracer.currentSpan()
        return span?.context()?.traceId() // 현재 Span의 TraceId 반환
    }
}