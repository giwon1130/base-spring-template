package com.template.base.application.service.auth

import com.template.base.infrastructure.security.exception.ErrorCode
import mu.KotlinLogging
import org.springframework.stereotype.Service
import com.template.base.infrastructure.security.JwtUtil
import com.template.base.infrastructure.security.exception.CustomException
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class RefreshTokenService(
    private val jwtUtil: JwtUtil
) {
    private val logger = KotlinLogging.logger {}
    private val refreshTokenStore = ConcurrentHashMap<String, RefreshTokenData>()
    private val refreshTokenTTLDays = 14L // Refresh Token 유효기간 (14일)

    data class RefreshTokenData(
        val userEmail: String,
        val expiresAt: LocalDateTime
    )

    /**
     * Refresh Token 검증 (인메모리 저장소에서 조회)
     */
    fun verifyRefreshToken(refreshToken: String): String {
        logger.info("Refresh Token 검증 요청 - refreshToken: {}", refreshToken)

        val tokenData = refreshTokenStore[refreshToken]
            ?: run {
                logger.warn("Refresh Token 검증 실패 - 존재하지 않는 토큰: {}", refreshToken)
                throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
            }

        if (tokenData.expiresAt.isBefore(LocalDateTime.now())) {
            logger.warn("Refresh Token 검증 실패 - 만료된 토큰: {}", refreshToken)
            refreshTokenStore.remove(refreshToken)
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        return tokenData.userEmail
    }

    /**
     * Refresh Token을 이용하여 새로운 Access Token 발급
     */
    fun generateAccessTokenFromRefreshToken(refreshToken: String): Pair<String, String> {
        logger.info("Access Token 갱신 요청 - Refresh Token: {}", refreshToken)

        val userEmail = verifyRefreshToken(refreshToken) // Refresh Token 검증

        val newAccessToken = jwtUtil.generateToken(userEmail)
        val newRefreshToken = createRefreshToken(userEmail) // 새로운 Refresh Token 생성

        logger.info("새로운 Access Token 및 Refresh Token 생성 완료 - email: {}", userEmail)

        deleteRefreshToken(refreshToken) // 기존 Refresh Token 삭제 (RTR 적용)
        logger.info("기존 Refresh Token 삭제 완료 - refreshToken: {}", refreshToken)

        return Pair(newAccessToken, newRefreshToken)
    }

    /**
     * 새로운 Refresh Token 생성 및 인메모리 저장
     */
    fun createRefreshToken(userEmail: String): String {
        val refreshToken = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusDays(refreshTokenTTLDays)

        logger.info("새로운 Refresh Token 생성 - email: {}, expiresAt: {}", userEmail, expiresAt)

        refreshTokenStore[refreshToken] = RefreshTokenData(userEmail, expiresAt)
        logger.info("Refresh Token 저장 완료 - refreshToken: {}, TTL: {} days", refreshToken, refreshTokenTTLDays)

        return refreshToken
    }

    /**
     * Refresh Token 삭제 (RTR 적용 또는 로그아웃 시)
     */
    fun deleteRefreshToken(refreshToken: String) {
        logger.info("Refresh Token 삭제 요청 - refreshToken: {}", refreshToken)

        refreshTokenStore.remove(refreshToken)
        logger.info("Refresh Token 삭제 완료 - refreshToken: {}", refreshToken)
    }

    /**
     * Refresh Token 무효화 (로그아웃 시 사용)
     */
    fun revokeRefreshToken(refreshToken: String) {
        logger.info("Refresh Token 무효화 요청 - refreshToken: {}", refreshToken)

        val removed = refreshTokenStore.remove(refreshToken)
        if (removed != null) {
            logger.info("Refresh Token 무효화 완료 - refreshToken: {}", refreshToken)
        } else {
            logger.warn("무효화할 Refresh Token을 찾을 수 없음 - refreshToken: {}", refreshToken)
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }
    }

    /**
     * 만료된 토큰 정리 (스케줄링 등으로 주기적 실행 권장)
     */
    fun cleanupExpiredTokens() {
        val now = LocalDateTime.now()
        val expiredTokens = refreshTokenStore.filterValues { it.expiresAt.isBefore(now) }.keys
        
        expiredTokens.forEach { refreshTokenStore.remove(it) }
        
        if (expiredTokens.isNotEmpty()) {
            logger.info("만료된 Refresh Token {} 개 정리 완료", expiredTokens.size)
        }
    }
}