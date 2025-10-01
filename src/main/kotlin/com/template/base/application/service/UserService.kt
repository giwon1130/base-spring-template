package com.template.base.application.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import com.template.base.application.mapper.UserMapper
import com.template.base.domain.repository.UserRepository
import com.template.base.infrastructure.security.exception.CustomException
import com.template.base.infrastructure.security.exception.ErrorCode
import com.template.base.presentation.dto.request.UpdateUserRequest
import com.template.base.presentation.dto.response.UserResponse

/**
 * 서비스의 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 사용자 정보 조회
     */
    fun getUserInfo(userId: Long): UserResponse {
        logger.info("사용자 정보 조회 요청 - userId: {}", userId)

        val user = userRepository.findById(userId).orElse(null)
        if (user == null) {
            logger.warn("사용자 정보 조회 실패 - 존재하지 않는 userId: {}", userId)
            throw CustomException(ErrorCode.USER_NOT_FOUND)
        }

        val userResponse = userMapper.toResponse(user)
        logger.info("사용자 정보 조회 완료 - userEmail: {}, name: {}", userResponse.email, userResponse.name)
        return userResponse
    }

    /**
     * 사용자 정보 수정
     */
    fun updateUserInfo(userId: Long, request: UpdateUserRequest): UserResponse {
        logger.info("사용자 정보 수정 요청 - userId: {}", userId)

        val user = userRepository.findById(userId).orElse(null)
        if (user == null) {
            logger.warn("사용자 정보 수정 실패 - 존재하지 않는 userId: {}", userId)
            throw CustomException(ErrorCode.USER_NOT_FOUND)
        }

        val updated = user.copy(name = request.name)
        val saved = userRepository.save(updated)
        
        logger.info("사용자 정보 수정 완료 - userId: {}, name: {}", saved.userId, saved.name)
        return userMapper.toResponse(saved)
    }

    /**
     * 이메일로 사용자 정보 조회
     */
    fun getUserInfoByEmail(email: String): UserResponse {
        logger.info("사용자 정보 조회 요청 - email: {}", email)

        val user = userRepository.findByEmail(email).orElse(null)
        if (user == null) {
            logger.warn("사용자 정보 조회 실패 - 존재하지 않는 email: {}", email)
            throw CustomException(ErrorCode.USER_NOT_FOUND)
        }

        val userResponse = userMapper.toResponse(user)
        logger.info("사용자 정보 조회 완료 - userEmail: {}, name: {}", userResponse.email, userResponse.name)
        return userResponse
    }
}