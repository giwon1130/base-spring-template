package com.template.platform.features.aoi.application.dto

import com.template.platform.features.aoi.domain.Aoi
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class AoiRequest(
    @field:NotBlank
    @field:Size(max = 30)
    val codeName: String,

    /**
     * SRID 4326 Polygon WKT. 예) POLYGON((126.0 37.0, ...))
     */
    @field:NotBlank
    val geometryWkt: String
)

data class AoiResponse(
    val aoiId: Long,
    val codeName: String,
    val geometryWkt: String,
    val createdAt: Instant?
) {
    companion object {
        fun from(aoi: Aoi, geometryWkt: String): AoiResponse = AoiResponse(
            aoiId = aoi.aoiId,
            codeName = aoi.codeName,
            geometryWkt = geometryWkt,
            createdAt = aoi.createdAt
        )
    }
}
