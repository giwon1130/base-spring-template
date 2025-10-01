package com.template.base.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.xml.bind.DatatypeConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.spec.SecretKeySpec

/**
 * JWT 토큰 생성 및 파싱을 담당하는 유틸리티 클래스
 */
@Service
class JwtUtil(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val expirationMs: Long
) {

    /**
     * 사용자 이메일을 기반으로 JWT 토큰을 생성합니다.
     */
    fun generateToken(email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * JWT 토큰을 파싱하여 클레임 정보를 반환합니다.
     */
    fun parseToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    /**
     * 토큰의 유효성을 검증합니다.
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = parseToken(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 토큰에서 사용자 이메일을 추출합니다.
     */
    fun getEmailFromToken(token: String): String {
        return parseToken(token).subject
    }

    private fun getSigningKey(): SecretKeySpec {
        val keyBytes = DatatypeConverter.parseBase64Binary(secretKey)
        return SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.jcaName)
    }
}