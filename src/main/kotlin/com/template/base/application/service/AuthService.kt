package com.template.base.application.service

import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.template.base.presentation.dto.request.RegisterRequest
import com.template.base.application.mapper.UserMapper
import com.template.base.domain.model.User
import com.template.base.domain.repository.UserRepository
import com.template.base.infrastructure.security.exception.CustomException
import com.template.base.infrastructure.security.exception.ErrorCode

/**
 * 인증 관련 로직을 처리하는 AuthService.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 회원가입
     */
    @Transactional
    fun registerUser(request: RegisterRequest): User {
        logger.info("회원가입 요청 - email: {}", request.email)

        if (userRepository.findByEmail(request.email).isPresent) {
            logger.warn("회원가입 실패 - 이미 존재하는 이메일: {}", request.email)
            throw CustomException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        val user = userMapper.toEntity(request, encodedPassword)
        val savedUser = userRepository.save(user)

        logger.info("회원가입 완료 - email: {}", savedUser.email)
        return savedUser
    }
}