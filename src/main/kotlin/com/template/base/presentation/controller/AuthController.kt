package com.template.base.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.template.base.application.service.AuthService
import com.template.base.application.service.auth.CommonAuthService
import com.template.base.presentation.dto.common.CommonResponse
import com.template.base.presentation.dto.request.LoginRequest
import com.template.base.presentation.dto.request.LogoutRequest
import com.template.base.presentation.dto.request.RefreshTokenRequest
import com.template.base.presentation.dto.request.RegisterRequest
import com.template.base.presentation.dto.response.LoginResponse

/**
 * 서비스의 인증 관련 API를 처리하는 컨트롤러.
 */
@RestController
@Tag(name = "Auth API", description = "인증 관련 API")
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val commonAuthService: CommonAuthService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 회원가입 API
     */
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록하는 API")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<CommonResponse<String>> {
        logger.info("회원가입 요청 - email: {}", request.email)

        authService.registerUser(request)
        return ResponseEntity.ok(CommonResponse(data = "회원가입이 완료되었습니다."))
    }

    /**
     * 로그인 API (JWT 토큰 발급)
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자가 이메일과 비밀번호로 로그인")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<CommonResponse<LoginResponse>> {
        logger.info("로그인 요청 - email: {}", request.email)

        val response = commonAuthService.login(request)

        logger.info("로그인 응답 - email: {}, accessToken 존재 여부: {}", request.email, response.accessToken != null)
        return ResponseEntity.ok(CommonResponse(data = response))
    }

    /**
     * Access Token 갱신 (RT 사용) API (JWT 토큰 발급)
     */
    @PostMapping("/refresh")
    @Operation(summary = "Access Token 갱신", description = "RT 토큰을 사용해서 AT, RT 갱신")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<CommonResponse<LoginResponse>> {
        logger.info("Access Token 갱신 요청 - Refresh Token: {}", request.refreshToken)

        val response = commonAuthService.refresh(request)

        logger.info("Access Token 갱신 완료 - 새로운 Access Token 발급됨")
        return ResponseEntity.ok(CommonResponse(data = response))
    }

    /**
     * 로그아웃 API (토큰 무효화)
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하여 로그아웃합니다")
    fun logout(@Valid @RequestBody request: LogoutRequest): ResponseEntity<CommonResponse<String>> {
        logger.info("로그아웃 요청 - Refresh Token: {}", request.refreshToken)

        commonAuthService.logout(request)

        logger.info("로그아웃 완료 - Refresh Token 무효화됨")
        return ResponseEntity.ok(CommonResponse(data = "로그아웃이 완료되었습니다."))
    }
}