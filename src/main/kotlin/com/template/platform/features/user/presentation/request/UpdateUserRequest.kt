package com.template.platform.features.user.presentation.request

import jakarta.validation.constraints.NotBlank

data class UpdateUserRequest(
    @field:NotBlank(message = "이름을 입력해주세요.")
    val name: String
)
