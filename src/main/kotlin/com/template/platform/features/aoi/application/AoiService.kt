package com.template.platform.features.aoi.application

import com.template.platform.common.error.CustomException
import com.template.platform.common.error.ErrorCode
import com.template.platform.common.util.GeometryUtils
import com.template.platform.features.aoi.application.dto.AoiRequest
import com.template.platform.features.aoi.application.dto.AoiResponse
import com.template.platform.features.aoi.domain.Aoi
import com.template.platform.features.aoi.domain.AoiRepository
import com.template.platform.features.aoi.domain.AoiSpecifications
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AoiService(
    private val aoiRepository: AoiRepository
) {

    @Transactional
    fun createAoi(request: AoiRequest): AoiResponse {
        if (aoiRepository.existsByCodeNameIgnoreCase(request.codeName)) {
            throw CustomException(ErrorCode.INVALID_REQUEST, "이미 존재하는 AOI 코드명입니다.")
        }

        val geometry = GeometryUtils.polygonFromWkt(request.geometryWkt)
            ?: throw CustomException(ErrorCode.INVALID_REQUEST, "AOI geometry는 필수입니다.")

        val aoi = Aoi(
            geometry = geometry,
            codeName = request.codeName
        )

        return AoiResponse.from(
            aoiRepository.save(aoi),
            GeometryUtils.toWkt(geometry)!!
        )
    }

    fun getAoi(aoiId: Long): AoiResponse {
        val entity = getAoiEntity(aoiId)
        return AoiResponse.from(entity, GeometryUtils.toWkt(entity.geometry)!!)
    }

    fun getAoiList(keyword: String?, pageable: Pageable): Page<AoiResponse> {
        var spec: Specification<Aoi> = Specification.where(null)
        AoiSpecifications.keywordLike(keyword)?.let { spec = spec.and(it) }

        return aoiRepository.findAll(spec, pageable)
            .map { AoiResponse.from(it, GeometryUtils.toWkt(it.geometry)!!) }
    }

    @Transactional
    fun updateAoi(aoiId: Long, request: AoiRequest): AoiResponse {
        val aoi = getAoiEntity(aoiId)

        if (!aoi.codeName.equals(request.codeName, ignoreCase = true) &&
            aoiRepository.existsByCodeNameIgnoreCase(request.codeName)
        ) {
            throw CustomException(ErrorCode.INVALID_REQUEST, "이미 존재하는 AOI 코드명입니다.")
        }

        val geometry = GeometryUtils.polygonFromWkt(request.geometryWkt)
            ?: throw CustomException(ErrorCode.INVALID_REQUEST, "AOI geometry는 필수입니다.")

        aoi.updateFrom(geometry, request.codeName)

        return AoiResponse.from(aoi, GeometryUtils.toWkt(aoi.geometry)!!)
    }

    @Transactional
    fun deleteAoi(aoiId: Long) {
        val aoi = getAoiEntity(aoiId)
        aoi.deletedAt = Instant.now()
    }

    private fun getAoiEntity(aoiId: Long): Aoi =
        aoiRepository.findById(aoiId).orElseThrow {
            CustomException(ErrorCode.ENTITY_NOT_FOUND, "AOI($aoiId)를 찾을 수 없습니다.")
        }
}
