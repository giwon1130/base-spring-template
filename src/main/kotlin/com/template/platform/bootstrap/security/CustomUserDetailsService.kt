package com.template.platform.bootstrap.security

import com.template.platform.features.user.domain.UserRepository
import mu.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * 이메일 기반 사용자 조회 구현체.
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    private val logger = KotlinLogging.logger {}

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found with email: $email") }

        logger.info { "인증된 사용자: ${user.email}" }
        return CustomUserDetails(user)
    }
}
