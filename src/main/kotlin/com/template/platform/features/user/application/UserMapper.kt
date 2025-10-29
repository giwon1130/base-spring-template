package com.template.platform.features.user.application

import com.template.platform.features.user.application.dto.UserDto
import com.template.platform.features.user.domain.User
import com.template.platform.features.user.presentation.request.RegisterRequest
import com.template.platform.features.user.presentation.response.UserResponse
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toEntity(request: RegisterRequest, encodedPassword: String): User {
        return User(
            email = request.email,
            password = encodedPassword,
            name = request.name,
            role = request.role
        )
    }

    fun toResponse(user: User): UserResponse {
        return UserResponse(
            email = user.email,
            name = user.name,
            role = user.role
        )
    }

    fun toDto(user: User): UserDto {
        return UserDto(
            email = user.email,
            password = user.password
        )
    }
}
