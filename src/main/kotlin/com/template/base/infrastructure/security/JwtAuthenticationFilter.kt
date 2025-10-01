package com.template.base.infrastructure.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.template.base.infrastructure.security.exception.CustomException
import com.template.base.infrastructure.security.exception.ErrorCode
import com.template.base.presentation.dto.response.ErrorResponse
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*


@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val publicMatchers = listOf(
        AntPathRequestMatcher("/api/v1/auth/**"),
        AntPathRequestMatcher("/api/v1/health/**"),
        AntPathRequestMatcher("/v3/api-docs/**"),
        AntPathRequestMatcher("/swagger-ui/**"),
        AntPathRequestMatcher("/swagger-ui.html"),
        AntPathRequestMatcher("/h2-console/**"),
        AntPathRequestMatcher("/error"),
        AntPathRequestMatcher("/actuator/**"),
        AntPathRequestMatcher("/**", "OPTIONS") // CORS preflight
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
            val token = resolveToken(request)

            if (token.isNullOrBlank()) {
                handleException(response, CustomException(ErrorCode.MISSING_ACCESS_TOKEN))
                return
            }

            val claims = jwtUtil.parseToken(token)
            if (claims.expiration.before(Date())) {
                throw CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN)
            }

            val username = claims.subject
            val userDetails: UserDetails = userDetailsService.loadUserByUsername(username)
            val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            SecurityContextHolder.getContext().authentication = auth

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

    private fun handleException(response: HttpServletResponse, e: CustomException) {
        response.status = e.errorCode.httpStatus.value()
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json;charset=UTF-8"

        val errorResponse = ErrorResponse.from(
            errorCode = e.errorCode,
            message = e.message,
            traceId = null,
            path = null
        )
        val objectMapper = jacksonObjectMapper()
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}