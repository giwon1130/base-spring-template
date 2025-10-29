package com.template.platform.features.scene.application.dto

import org.springframework.core.io.Resource
import org.springframework.http.MediaType

data class FileResourceResult(
    val resource: Resource,
    val filename: String,
    val mediaType: MediaType
)
