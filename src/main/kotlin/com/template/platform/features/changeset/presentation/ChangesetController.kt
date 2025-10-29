package com.template.platform.features.changeset.presentation

import com.template.platform.common.error.CustomException
import com.template.platform.common.error.ErrorCode
import com.template.platform.common.geo.GeoUtils
import com.template.platform.common.response.CommonResponse
import com.template.platform.common.geo.BBox
import com.template.platform.features.changeset.application.ChangesetDto
import com.template.platform.features.changeset.application.ChangesetService
import com.template.platform.features.changeset.application.CreateChangesetRequest
import com.template.platform.features.changeset.domain.ChangeType
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*

/**
 * 변경사항 API 컨트롤러
 */
@RestController
@RequestMapping("/api/changes")
class ChangesetController(
    private val changesetService: ChangesetService
) {
    private val logger = KotlinLogging.logger {}
    
    /**
     * BBOX 영역 내 변경사항 조회
     * 
     * @param minx 최소 경도
     * @param miny 최소 위도
     * @param maxx 최대 경도
     * @param maxy 최대 위도
     */
    @GetMapping
    fun getChangesByBBox(
        @RequestParam minx: Double,
        @RequestParam miny: Double,
        @RequestParam maxx: Double,
        @RequestParam maxy: Double
    ): CommonResponse<List<ChangesetDto>> {
        logger.info { "Querying changes for BBOX: ($minx, $miny, $maxx, $maxy)" }
        
        // BBOX 검증
        if (!GeoUtils.isValidBBox(minx, miny, maxx, maxy)) {
            throw CustomException(ErrorCode.CHANGESET_INVALID_BBOX)
        }
        
        val bbox = BBox(minx, miny, maxx, maxy)
        val changes = changesetService.findByBBox(bbox)
        
        return CommonResponse.success(
            data = changes,
            message = "Found ${changes.size} changes in the specified area"
        )
    }
    
    /**
     * 모든 변경사항 조회 (캐시 적용)
     */
    @GetMapping("/all")
    fun getAllChanges(): CommonResponse<List<ChangesetDto>> {
        logger.info { "Querying all changes" }
        
        val changes = changesetService.findAll()
        return CommonResponse.success(
            data = changes,
            message = "Found ${changes.size} total changes"
        )
    }
    
    /**
     * 새 변경사항 생성
     */
    @PostMapping
    fun createChange(@RequestBody request: CreateChangeRequest): CommonResponse<ChangesetDto> {
        logger.info { "Creating new changeset: ${request.title}" }
        
        val serviceRequest = CreateChangesetRequest(
            title = request.title,
            description = request.description,
            changeType = request.changeType,
            geometry = request.geometryWkt?.let { GeoUtils.parseWKT(it) as org.locationtech.jts.geom.Polygon },
            createdBy = request.createdBy,
            metadata = request.metadata
        )
        
        val created = changesetService.create(serviceRequest)
        return CommonResponse.success(
            data = created,
            message = "Changeset created successfully"
        )
    }
    
    /**
     * 변경사항 통계 조회
     */
    @GetMapping("/statistics")
    fun getStatistics(): CommonResponse<Map<ChangeType, Long>> {
        logger.info { "Querying changeset statistics" }
        
        val stats = changesetService.getStatistics()
        return CommonResponse.success(
            data = stats,
            message = "Statistics for the last 30 days"
        )
    }
}

/**
 * 변경사항 생성 요청 DTO
 */
data class CreateChangeRequest(
    val title: String,
    val description: String? = null,
    val changeType: ChangeType,
    val geometryWkt: String? = null,
    val createdBy: String,
    val metadata: String? = null
)
