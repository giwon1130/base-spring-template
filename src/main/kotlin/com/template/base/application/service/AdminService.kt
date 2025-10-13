package com.template.base.application.service

import mu.KotlinLogging
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.template.base.application.mapper.UserMapper
import com.template.base.domain.repository.UserRepository
import com.template.base.infrastructure.security.exception.CustomException
import com.template.base.infrastructure.security.exception.ErrorCode
import com.template.base.presentation.dto.common.PageRequest
import com.template.base.presentation.dto.common.PageResponse
import com.template.base.presentation.dto.request.AdminUserUpdateRequest
import com.template.base.presentation.dto.response.UserResponse
import java.time.Instant

/**
 * 관리자 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@Transactional
class AdminService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 사용자 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    fun getUsers(pageRequest: PageRequest, search: String?): PageResponse<UserResponse> {
        logger.info("사용자 목록 조회 시작 - page: {}, size: {}, search: {}", pageRequest.page, pageRequest.size, search)

        val sort = parseSort(pageRequest.sort)
        val pageable = SpringPageRequest.of(pageRequest.page, pageRequest.size, sort)

        val page = if (search.isNullOrBlank()) {
            userRepository.findAllIncludingDeleted(pageable)
        } else {
            userRepository.findByEmailContainingOrNameContainingIncludingDeleted(search, search, pageable)
        }

        val users = page.content.map { userMapper.toUserResponse(it) }

        return PageResponse(
            content = users,
            page = PageResponse.PageInfo(
                number = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                first = page.isFirst,
                last = page.isLast,
                empty = page.isEmpty
            )
        )
    }

    /**
     * 특정 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    fun getUser(userId: Long): UserResponse {
        logger.info("사용자 정보 조회 시작 - userId: {}", userId)

        val user = userRepository.findByIdIncludingDeleted(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return userMapper.toUserResponse(user)
    }

    /**
     * 사용자 정보 수정 (관리자용)
     */
    fun updateUser(userId: Long, request: AdminUserUpdateRequest): UserResponse {
        logger.info("관리자용 사용자 정보 수정 시작 - userId: {}", userId)

        val user = userRepository.findByIdIncludingDeleted(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 업데이트할 필드들만 변경하여 새 인스턴스 생성
        val updatedUser = user.copy(
            name = request.name ?: user.name,
            role = request.role ?: user.role
        )

        // 활성화 상태 변경
        request.active?.let { active ->
            if (active && user.deletedAt != null) {
                // 계정 활성화
                updatedUser.deletedAt = null
                logger.info("사용자 계정 활성화 - userId: {}", userId)
            } else if (!active && user.deletedAt == null) {
                // 계정 비활성화
                updatedUser.deletedAt = Instant.now()
                logger.info("사용자 계정 비활성화 - userId: {}", userId)
            }
        }

        val savedUser = userRepository.save(updatedUser)
        logger.info("관리자용 사용자 정보 수정 완료 - userId: {}", userId)

        return userMapper.toUserResponse(savedUser)
    }

    /**
     * 사용자 계정 비활성화 (소프트 삭제)
     */
    fun deactivateUser(userId: Long) {
        logger.info("사용자 계정 비활성화 시작 - userId: {}", userId)

        val user = userRepository.findById(userId).orElseThrow {
            CustomException(ErrorCode.USER_NOT_FOUND)
        }

        if (user.deletedAt != null) {
            throw CustomException(ErrorCode.USER_ALREADY_DELETED)
        }

        user.deletedAt = Instant.now()
        userRepository.save(user)

        logger.info("사용자 계정 비활성화 완료 - userId: {}", userId)
    }

    /**
     * 사용자 계정 활성화
     */
    fun activateUser(userId: Long) {
        logger.info("사용자 계정 활성화 시작 - userId: {}", userId)

        val user = userRepository.findByIdIncludingDeleted(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        if (user.deletedAt == null) {
            throw CustomException(ErrorCode.USER_ALREADY_ACTIVE)
        }

        user.deletedAt = null
        userRepository.save(user)

        logger.info("사용자 계정 활성화 완료 - userId: {}", userId)
    }

    /**
     * 정렬 파라미터 파싱
     */
    private fun parseSort(sortParam: String?): Sort {
        if (sortParam.isNullOrBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt")
        }

        return try {
            val parts = sortParam.split(",")
            val property = parts[0]
            val direction = if (parts.size > 1 && parts[1].equals("asc", ignoreCase = true)) {
                Sort.Direction.ASC
            } else {
                Sort.Direction.DESC
            }
            Sort.by(direction, property)
        } catch (e: Exception) {
            logger.warn("정렬 파라미터 파싱 실패: {}, 기본값 사용", sortParam)
            Sort.by(Sort.Direction.DESC, "createdAt")
        }
    }
}