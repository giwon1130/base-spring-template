package com.template.platform.features.user.application

import com.template.platform.common.error.CustomException
import com.template.platform.common.error.ErrorCode
import com.template.platform.features.user.domain.User
import com.template.platform.features.user.domain.UserRepository
import com.template.platform.features.user.presentation.request.RegisterRequest
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper
) {

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun registerUser(request: RegisterRequest): User {
        logger.info { "회원가입 요청 - email=${request.email}" }

        if (userRepository.findByEmail(request.email).isPresent) {
            logger.warn { "회원가입 실패 - 중복 이메일: ${request.email}" }
            throw CustomException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        val user = userMapper.toEntity(request, encodedPassword)
        val saved = userRepository.save(user)

        logger.info { "회원가입 완료 - userId=${saved.userId}" }
        return saved
    }
}
