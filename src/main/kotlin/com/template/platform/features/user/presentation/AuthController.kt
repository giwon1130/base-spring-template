package com.template.platform.features.user.presentation

import com.template.platform.common.response.CommonResponse
import com.template.platform.features.user.application.AuthService
import com.template.platform.features.user.application.CommonAuthService
import com.template.platform.features.user.presentation.request.LoginRequest
import com.template.platform.features.user.presentation.request.RefreshTokenRequest
import com.template.platform.features.user.presentation.request.RegisterRequest
import com.template.platform.features.user.presentation.response.LoginResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val commonAuthService: CommonAuthService
) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<CommonResponse<String>> {
        logger.info { "회원가입 요청 - email=${request.email}" }
        authService.registerUser(request)
        return ResponseEntity.ok(CommonResponse(data = "회원가입이 완료되었습니다."))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<CommonResponse<LoginResponse>> {
        logger.info { "로그인 요청 - email=${request.email}" }
        val response = commonAuthService.login(request)
        return ResponseEntity.ok(CommonResponse(data = response))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<CommonResponse<LoginResponse>> {
        logger.info { "Access Token 갱신 요청" }
        val response = commonAuthService.refresh(request)
        return ResponseEntity.ok(CommonResponse(data = response))
    }
}
