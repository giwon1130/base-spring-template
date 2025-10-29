package com.template.platform.features.scene.infrastructure

import com.template.platform.common.util.PresignedUrlProvider
import com.template.platform.features.scene.application.SceneFileService
import com.template.platform.features.scene.application.dto.FileResourceResult
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SceneFileServiceImpl(
    private val presignedUrlProvider: PresignedUrlProvider
) : SceneFileService {

    override fun generateSceneDownloadUrl(changeDetectionId: Long): String {
        val path = "/api/v1/scenes/change-detections/$changeDetectionId/download"
        return presignedUrlProvider.createUrl(path, changeDetectionId)
    }

    override fun getSceneFile(changeDetectionId: Long, expires: Long, signature: String): FileResourceResult {
        val path = "/api/v1/scenes/change-detections/$changeDetectionId/download"
        presignedUrlProvider.validate(path, changeDetectionId, expires, signature)
        val payload = "Scene file placeholder for changeDetectionId=$changeDetectionId generated at ${Instant.now()}"
        val resource = ByteArrayResource(payload.toByteArray())
        return FileResourceResult(
            resource = resource,
            filename = "scene-$changeDetectionId.txt",
            mediaType = MediaType.TEXT_PLAIN
        )
    }

    override fun generateLabelDownloadUrl(changeDetectionId: Long): String {
        val path = "/api/v1/scenes/change-detections/$changeDetectionId/labels/download"
        return presignedUrlProvider.createUrl(path, changeDetectionId)
    }

    override fun getLabelFile(changeDetectionId: Long, expires: Long, signature: String): FileResourceResult {
        val path = "/api/v1/scenes/change-detections/$changeDetectionId/labels/download"
        presignedUrlProvider.validate(path, changeDetectionId, expires, signature)
        val geoJson = """
            {
              "type": "FeatureCollection",
              "features": []
            }
        """.trimIndent()
        val resource = ByteArrayResource(geoJson.toByteArray())
        return FileResourceResult(
            resource = resource,
            filename = "scene-$changeDetectionId-labels.geojson",
            mediaType = MediaType.APPLICATION_JSON
        )
    }
}
