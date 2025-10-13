package com.template.base.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.template.base.presentation.dto.request.ChangePasswordRequest
import com.template.base.presentation.dto.request.UpdateUserRequest
import com.template.base.application.service.UserService
import com.template.base.infrastructure.security.CustomUserDetails
import com.template.base.presentation.dto.common.CommonResponse
import com.template.base.presentation.dto.response.UserResponse

/**
 * 사용자 관련 API를 처리하는 컨트롤러.
 */
@RestController
@Tag(name = "User API", description = "사용자 관련 API")
@RequestMapping("/api/v1/user")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 내 정보 조회 API
     */
    @GetMapping("/me")
    @Operation(summary = "내정보 조회", description = "내 정보를 조회하는 API")
    fun getUserInfo(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<CommonResponse<UserResponse>> {
        logger.info("내 정보 조회 요청 - userId: {}", userDetails.getUserId())

        val userResponse = userService.getUserInfo(userDetails.getUserId())

        logger.info("내 정보 조회 완료 - userId: {}, name: {}", userResponse.email, userResponse.name)
        return ResponseEntity.ok(CommonResponse(data = userResponse))
    }

    /**
     * 내 정보 수정 API
     */
    @PutMapping("/me")
    @Operation(summary = "내정보 수정", description = "내 정보를 수정하는 API")
    fun updateUserInfo(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: UpdateUserRequest,
    ): ResponseEntity<CommonResponse<UserResponse>> {
        logger.info("내 정보 수정 요청 - userId: {}", userDetails.getUserId())

        val updated = userService.updateUserInfo(userDetails.getUserId(), request)

        logger.info("내 정보 수정 완료 - userId: {}, name: {}", updated.email, updated.name)
        return ResponseEntity.ok(CommonResponse(data = updated))
    }

    /**
     * 비밀번호 변경 API
     */
    @PostMapping("/change-password")
    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경하는 API")
    fun changePassword(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<CommonResponse<String>> {
        logger.info("비밀번호 변경 요청 - userId: {}", userDetails.getUserId())

        userService.changePassword(userDetails.getUserId(), request)

        logger.info("비밀번호 변경 완료 - userId: {}", userDetails.getUserId())
        return ResponseEntity.ok(CommonResponse(data = "비밀번호가 성공적으로 변경되었습니다."))
    }
}