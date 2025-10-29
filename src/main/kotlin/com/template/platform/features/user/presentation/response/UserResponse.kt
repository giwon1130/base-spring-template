package com.template.platform.features.user.presentation.response

import com.template.platform.features.user.domain.Role

data class UserResponse(
    val email: String,
    val name: String,
    val role: Role
)
