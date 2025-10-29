package com.template.platform.features.user.presentation.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val error: String? = null
)
