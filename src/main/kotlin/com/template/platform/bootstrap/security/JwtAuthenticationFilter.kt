package com.template.platform.bootstrap.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.template.platform.common.error.CustomException
import com.template.platform.common.error.ErrorCode
import com.template.platform.common.response.ErrorResponse
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Date

/**
 * JWT 기반 인증 필터.
 *
 * BMOA에서 사용하던 공개 엔드포인트 정의를 유지하면서 템플릿 서비스에 맞게 패키지만 조정했다.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val logger = KotlinLogging.logger {}

    private val publicMatchers = listOf(
        AntPathRequestMatcher("/api/v1/auth/**"),
        AntPathRequestMatcher("/api/v1/errors"),
        AntPathRequestMatcher("/api/v1/label-render/**"),
        AntPathRequestMatcher("/api/v1/notifications/**"),
        AntPathRequestMatcher("/api/v1/scenes/change-detections/*/download", "GET"),
        AntPathRequestMatcher("/api/v1/scenes/change-detections/*/labels/download", "GET"),
        AntPathRequestMatcher("/api/v1/reports/download-secure", "GET"),
        AntPathRequestMatcher("/v3/api-docs/**"),
        AntPathRequestMatcher("/swagger-ui/**"),
        AntPathRequestMatcher("/error"),
        AntPathRequestMatcher("/**", "OPTIONS")
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return publicMatchers.any { it.matches(request) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = resolveToken(request) ?: run {
                logger.debug { "Authorization header missing on protected endpoint: ${request.requestURI}" }
                handleException(response, CustomException(ErrorCode.MISSING_ACCESS_TOKEN))
                return
            }

            val claims = jwtUtil.parseToken(token)
            if (claims.expiration.before(Date())) {
                throw CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN)
            }

            val username = claims.subject
            val userDetails: UserDetails = userDetailsService.loadUserByUsername(username)
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities
            )
            SecurityContextHolder.getContext().authentication = authentication

            filterChain.doFilter(request, response)
        } catch (e: ExpiredJwtException) {
            handleException(response, CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN))
        } catch (e: io.jsonwebtoken.JwtException) {
            handleException(response, CustomException(ErrorCode.INVALID_ACCESS_TOKEN))
        } catch (e: CustomException) {
            handleException(response, e)
        } catch (e: Exception) {
            handleException(response, CustomException(ErrorCode.INVALID_ACCESS_TOKEN))
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null

        if (!header.startsWith("Bearer ")) {
            throw CustomException(ErrorCode.INVALID_AUTH_HEADER)
        }

        val token = header.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            throw CustomException(ErrorCode.INVALID_AUTH_HEADER)
        }
        return token
    }

    private fun handleException(response: HttpServletResponse, exception: CustomException) {
        response.status = exception.errorCode.httpStatus.value()
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json;charset=UTF-8"

        val errorResponse = ErrorResponse.from(
            errorCode = exception.errorCode,
            message = exception.message,
            traceId = null,
            path = null
        )

        val objectMapper = jacksonObjectMapper()
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
