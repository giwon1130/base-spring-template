package com.template.platform.features.scene.application

import com.template.platform.features.scene.application.dto.FileResourceResult

interface SceneFileService {
    fun generateSceneDownloadUrl(changeDetectionId: Long): String
    fun getSceneFile(changeDetectionId: Long, expires: Long, signature: String): FileResourceResult

    fun generateLabelDownloadUrl(changeDetectionId: Long): String
    fun getLabelFile(changeDetectionId: Long, expires: Long, signature: String): FileResourceResult
}
