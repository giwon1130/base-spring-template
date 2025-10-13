package com.template.base.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import com.template.base.application.service.AdminService
import com.template.base.presentation.dto.common.CommonResponse
import com.template.base.presentation.dto.common.PageRequest
import com.template.base.presentation.dto.common.PageResponse
import com.template.base.presentation.dto.request.AdminUserUpdateRequest
import com.template.base.presentation.dto.response.UserResponse

/**
 * 관리자 전용 API를 처리하는 컨트롤러
 */
@RestController
@Tag(name = "Admin API", description = "관리자 전용 API")
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearerAuth")
class AdminController(
    private val adminService: AdminService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 사용자 목록 조회 (페이지네이션)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 목록 조회", description = "페이지네이션을 적용한 사용자 목록을 조회합니다")
    fun getUsers(
        @Valid pageRequest: PageRequest,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<CommonResponse<PageResponse<UserResponse>>> {
        logger.info("사용자 목록 조회 요청 - page: {}, size: {}, search: {}", pageRequest.page, pageRequest.size, search)

        val users = adminService.getUsers(pageRequest, search)

        logger.info("사용자 목록 조회 완료 - 총 {}명", users.page.totalElements)
        return ResponseEntity.ok(CommonResponse(data = users))
    }

    /**
     * 특정 사용자 정보 조회
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 상세 정보를 조회합니다")
    fun getUser(@PathVariable userId: Long): ResponseEntity<CommonResponse<UserResponse>> {
        logger.info("사용자 정보 조회 요청 - userId: {}", userId)

        val user = adminService.getUser(userId)

        logger.info("사용자 정보 조회 완료 - userId: {}, email: {}", userId, user.email)
        return ResponseEntity.ok(CommonResponse(data = user))
    }

    /**
     * 사용자 정보 수정 (관리자용)
     */
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 정보 수정", description = "관리자가 사용자 정보를 수정합니다")
    fun updateUser(
        @PathVariable userId: Long,
        @Valid @RequestBody request: AdminUserUpdateRequest
    ): ResponseEntity<CommonResponse<UserResponse>> {
        logger.info("사용자 정보 수정 요청 - userId: {}", userId)

        val updatedUser = adminService.updateUser(userId, request)

        logger.info("사용자 정보 수정 완료 - userId: {}, email: {}", userId, updatedUser.email)
        return ResponseEntity.ok(CommonResponse(data = updatedUser))
    }

    /**
     * 사용자 계정 비활성화 (소프트 삭제)
     */
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 계정 비활성화", description = "사용자 계정을 비활성화합니다 (소프트 삭제)")
    fun deactivateUser(@PathVariable userId: Long): ResponseEntity<CommonResponse<String>> {
        logger.info("사용자 계정 비활성화 요청 - userId: {}", userId)

        adminService.deactivateUser(userId)

        logger.info("사용자 계정 비활성화 완료 - userId: {}", userId)
        return ResponseEntity.ok(CommonResponse(data = "사용자 계정이 비활성화되었습니다."))
    }

    /**
     * 사용자 계정 활성화
     */
    @PostMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 계정 활성화", description = "비활성화된 사용자 계정을 다시 활성화합니다")
    fun activateUser(@PathVariable userId: Long): ResponseEntity<CommonResponse<String>> {
        logger.info("사용자 계정 활성화 요청 - userId: {}", userId)

        adminService.activateUser(userId)

        logger.info("사용자 계정 활성화 완료 - userId: {}", userId)
        return ResponseEntity.ok(CommonResponse(data = "사용자 계정이 활성화되었습니다."))
    }
}