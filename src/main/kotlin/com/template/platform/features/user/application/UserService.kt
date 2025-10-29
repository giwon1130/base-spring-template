package com.template.platform.features.user.application

import com.template.platform.common.error.CustomException
import com.template.platform.common.error.ErrorCode
import com.template.platform.features.user.domain.UserRepository
import com.template.platform.features.user.presentation.request.UpdateUserRequest
import com.template.platform.features.user.presentation.response.UserResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {

    private val logger = KotlinLogging.logger {}

    fun getUserInfo(userId: Long): UserResponse {
        logger.info { "사용자 정보 조회 요청 - userId=$userId" }

        val user = userRepository.findById(userId).orElseThrow {
            logger.warn { "사용자 정보 조회 실패 - 존재하지 않는 userId=$userId" }
            CustomException(ErrorCode.USER_NOT_FOUND)
        }

        val response = userMapper.toResponse(user)
        logger.info { "사용자 정보 조회 완료 - email=${response.email}, name=${response.name}" }
        return response
    }

    fun updateUserInfo(userId: Long, request: UpdateUserRequest): UserResponse {
        logger.info { "사용자 정보 수정 요청 - userId=$userId" }

        val user = userRepository.findById(userId).orElseThrow {
            logger.warn { "사용자 정보 수정 실패 - 존재하지 않는 userId=$userId" }
            CustomException(ErrorCode.USER_NOT_FOUND)
        }

        val updated = user.copy(name = request.name)
        val saved = userRepository.save(updated)

        logger.info { "사용자 정보 수정 완료 - userId=${saved.userId}, name=${saved.name}" }
        return userMapper.toResponse(saved)
    }
}
