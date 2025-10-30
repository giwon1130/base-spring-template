package com.template.platform.features.user.presentation

import com.template.platform.bootstrap.security.CustomUserDetails
import com.template.platform.common.response.CommonResponse
import com.template.platform.features.user.application.UserService
import com.template.platform.features.user.presentation.request.UpdateUserRequest
import com.template.platform.features.user.presentation.response.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User API", description = "사용자 관련 API")
class UserController(
    private val userService: UserService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    fun getUserInfo(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): CommonResponse<UserResponse> {
        logger.info { "내 정보 조회 요청 - userId=${userDetails.getUserId()}" }

        val userResponse = userService.getUserInfo(userDetails.getUserId())

        logger.info { "내 정보 조회 완료 - email=${userResponse.email}, name=${userResponse.name}" }
        return CommonResponse.success(data = userResponse)
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 이름을 변경합니다.")
    fun updateUserInfo(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: UpdateUserRequest
    ): CommonResponse<UserResponse> {
        logger.info { "내 정보 수정 요청 - userId=${userDetails.getUserId()}" }

        val updated = userService.updateUserInfo(userDetails.getUserId(), request)

        logger.info { "내 정보 수정 완료 - email=${updated.email}, name=${updated.name}" }
        return CommonResponse.success(data = updated)
    }
}
