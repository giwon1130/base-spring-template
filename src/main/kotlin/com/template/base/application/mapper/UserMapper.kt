package com.template.base.application.mapper

import com.template.base.presentation.dto.request.RegisterRequest
import com.template.base.domain.model.User
import org.springframework.stereotype.Component
import com.template.base.presentation.dto.response.UserResponse
import com.template.base.application.dto.UserDto

/**
 * User Entity ↔ DTO 변환을 담당하는 Mapper
 */
@Component
class UserMapper {

    /**
     * 회원가입 요청 DTO → User 엔티티 변환
     */
    fun toEntity(request: RegisterRequest, encodedPassword: String): User {
        return User(
            email = request.email,
            password = encodedPassword,
            name = request.name,
            role = request.role
        )
    }

    /**
     * User 엔티티 → User 응답 DTO 변환
     */
    fun toResponse(user: User): UserResponse {
        return UserResponse(
            email = user.email,
            name = user.name,
            role = user.role
        )
    }

    /**
     * User 엔티티 → UserDto 변환 (인증용)
     */
    fun toDto(user: User): UserDto {
        return UserDto(
            email = user.email,
            password = user.password
        )
    }
}