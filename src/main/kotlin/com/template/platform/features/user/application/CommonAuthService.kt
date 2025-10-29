package com.template.platform.features.user.application

import com.template.platform.bootstrap.security.JwtUtil
import com.template.platform.common.error.CustomException
import com.template.platform.common.error.ErrorCode
import com.template.platform.features.user.domain.UserRepository
import com.template.platform.features.user.presentation.request.LoginRequest
import com.template.platform.features.user.presentation.request.RefreshTokenRequest
import com.template.platform.features.user.presentation.response.LoginResponse
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

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
        logger.info { "로그인 요청 - email=${request.email}" }

        val userDto = userRepository.findByEmail(request.email)
            .map(userMapper::toDto)
            .orElseThrow {
                logger.warn { "로그인 실패 - 존재하지 않는 email=${request.email}" }
                CustomException(ErrorCode.INVALID_CREDENTIALS)
            }

        if (!passwordEncoder.matches(request.password, userDto.password)) {
            logger.warn { "로그인 실패 - 비밀번호 불일치 email=${request.email}" }
            throw CustomException(ErrorCode.INVALID_CREDENTIALS)
        }

        val accessToken = jwtUtil.generateToken(userDto.email)
        val refreshToken = refreshTokenService.createRefreshToken(userDto.email)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun refresh(request: RefreshTokenRequest): LoginResponse {
        logger.info { "Access Token 갱신 요청" }

        val (newAccessToken, newRefreshToken) = refreshTokenService.generateAccessTokenFromRefreshToken(request.refreshToken)

        return LoginResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
}
