package com.template.platform.features.user.application

import com.template.platform.bootstrap.security.JwtUtil
import com.template.platform.common.error.CustomException
import com.template.platform.common.error.ErrorCode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class RefreshTokenService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtUtil: JwtUtil,
    @Value("\${spring.data.redis.key.refresh-token}") private val redisPrefix: String
) {

    private val logger = KotlinLogging.logger {}
    private val refreshTokenTTL = Duration.ofDays(14)

    fun verifyRefreshToken(refreshToken: String): String {
        val redisKey = "$redisPrefix$refreshToken"
        logger.info { "Refresh Token 검증 요청 - redisKey=$redisKey" }

        return redisTemplate.opsForValue().get(redisKey)
            ?: run {
                logger.warn { "Refresh Token 검증 실패 - redisKey=$redisKey" }
                throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
            }
    }

    fun generateAccessTokenFromRefreshToken(refreshToken: String): Pair<String, String> {
        logger.info { "Access Token 갱신 요청" }

        val userEmail = verifyRefreshToken(refreshToken)
        val newAccessToken = jwtUtil.generateToken(userEmail)
        val newRefreshToken = createRefreshToken(userEmail)

        deleteRefreshToken(refreshToken)

        return newAccessToken to newRefreshToken
    }

    fun createRefreshToken(userEmail: String): String {
        val refreshToken = UUID.randomUUID().toString()
        val redisKey = "$redisPrefix$refreshToken"

        redisTemplate.opsForValue().set(redisKey, userEmail, refreshTokenTTL)
        logger.info { "Refresh Token 저장 - redisKey=$redisKey, TTL=${refreshTokenTTL.toDays()}days" }

        return refreshToken
    }

    fun deleteRefreshToken(refreshToken: String) {
        val redisKey = "$redisPrefix$refreshToken"
        redisTemplate.delete(redisKey)
        logger.info { "Refresh Token 삭제 - redisKey=$redisKey" }
    }
}
