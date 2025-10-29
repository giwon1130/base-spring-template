package com.template.platform.features.scene.application.dto

import com.template.platform.common.util.GeometryUtils
import com.template.platform.features.scene.domain.Scene
import com.template.platform.features.scene.domain.SceneStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.Instant

data class SceneRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    @field:NotNull
    val imageCreatedAt: Instant,

    @field:NotBlank
    @field:Size(max = 100)
    val province: String,

    @field:NotBlank
    @field:Size(max = 100)
    val district: String,

    @field:Positive
    val totalSize: Long,

    val cogFilePath: String? = null,
    val geotransform: List<Double>? = null,
    val projection: String? = null,
    val geodeticPolygonWkt: String? = null,
    val status: SceneStatus = SceneStatus.PENDING,
    val gsd: Double? = null,
    val stacUrl: String? = null
)

data class SceneResponse(
    val sceneId: Long,
    val name: String,
    val imageCreatedAt: Instant,
    val province: String,
    val district: String,
    val totalSize: Long,
    val cogFilePath: String?,
    val geotransform: List<Double>?,
    val projection: String?,
    val geodeticPolygonWkt: String?,
    val status: SceneStatus,
    val gsd: Double?,
    val stacUrl: String?,
    val createdAt: Instant?,
    val lastModifiedAt: Instant?
) {
    companion object {
        fun from(scene: Scene): SceneResponse = SceneResponse(
            sceneId = scene.sceneId,
            name = scene.name,
            imageCreatedAt = scene.imageCreatedAt,
            province = scene.province,
            district = scene.district,
            totalSize = scene.totalSize,
            cogFilePath = scene.cogFilePath,
            geotransform = scene.geotransform?.toList(),
            projection = scene.projection,
            geodeticPolygonWkt = GeometryUtils.toWkt(scene.geodeticPolygon),
            status = scene.status,
            gsd = scene.gsd,
            stacUrl = scene.stacUrl,
            createdAt = scene.createdAt,
            lastModifiedAt = scene.lastModifiedAt
        )
    }
}

data class SceneCountResponse(
    val count: Long
)
