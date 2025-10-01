package com.template.base.application.service.auth

import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import com.template.base.presentation.dto.request.LoginRequest
import com.template.base.presentation.dto.request.RefreshTokenRequest
import com.template.base.application.mapper.UserMapper
import com.template.base.domain.repository.UserRepository
import com.template.base.infrastructure.security.JwtUtil
import com.template.base.infrastructure.security.exception.CustomException
import com.template.base.infrastructure.security.exception.ErrorCode
import com.template.base.presentation.dto.response.LoginResponse

@Service
class CommonAuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val refreshTokenService: RefreshTokenService,
    private val userMapper: UserMapper
) {
    private val logger = KotlinLogging.logger {}

    fun login(request: LoginRequest): LoginResponse {
        logger.info("로그인 요청 - email: {}", request.email)

        val userDto = userRepository.findByEmail(request.email)
            .map(userMapper::toDto)
            .orElseThrow {
                logger.warn("로그인 실패 - 존재하지 않는 email: {}", request.email)
                CustomException(ErrorCode.INVALID_CREDENTIALS)
            }

        if (!passwordEncoder.matches(request.password, userDto.password)) {
            logger.warn("로그인 실패 - 비밀번호 불일치 - email: {}", request.email)
            throw CustomException(ErrorCode.INVALID_CREDENTIALS)
        }

        val accessToken = jwtUtil.generateToken(userDto.email)
        val refreshToken = refreshTokenService.createRefreshToken(userDto.email)

        logger.info("로그인 성공 - email: {}", request.email)
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun refresh(request: RefreshTokenRequest): LoginResponse {
        logger.info("Access Token 갱신 요청 - Refresh Token: {}", request.refreshToken)

        val (newAccessToken, newRefreshToken) = refreshTokenService.generateAccessTokenFromRefreshToken(request.refreshToken)

        logger.info("Access Token 갱신 완료 - 새로운 Access Token 발급됨")
        return LoginResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
}