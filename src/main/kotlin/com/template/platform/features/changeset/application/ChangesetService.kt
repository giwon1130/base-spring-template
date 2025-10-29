package com.template.platform.features.changeset.application

import com.template.platform.common.cache.CacheManager
import com.template.platform.common.geo.BBox
import com.template.platform.features.changeset.domain.Changeset
import com.template.platform.features.changeset.domain.ChangeType
import com.template.platform.features.changeset.infrastructure.ChangesetRepository
import mu.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 변경사항 서비스
 * 
 * 캐시 적용 및 무효화 포함
 */
@Service
@Transactional(readOnly = true)
class ChangesetService(
    private val changesetRepository: ChangesetRepository,
    private val cacheManager: CacheManager
) {
    private val logger = KotlinLogging.logger {}
    
    companion object {
        const val CACHE_NAME = "changeset"
    }
    
    /**
     * BBOX 영역 내 변경사항 조회 (캐시 적용)
     */
    @Cacheable(value = [CACHE_NAME], key = "#bbox.toCacheKey('changeset')")
    fun findByBBox(bbox: BBox): List<ChangesetDto> {
        logger.debug { "Querying changesets for BBOX: $bbox" }
        
        val changesets = changesetRepository.findByBBox(
            bbox.minX, bbox.minY, bbox.maxX, bbox.maxY
        )
        
        return changesets.map { it.toDto() }
    }
    
    /**
     * 모든 변경사항 조회 (캐시 적용)
     */
    @Cacheable(value = [CACHE_NAME], key = "'all'")
    fun findAll(): List<ChangesetDto> {
        logger.debug { "Querying all changesets" }
        return changesetRepository.findAll().map { it.toDto() }
    }
    
    /**
     * 변경사항 생성
     */
    @Transactional
    fun create(request: CreateChangesetRequest): ChangesetDto {
        logger.info { "Creating changeset: ${request.title}" }
        
        val changeset = Changeset(
            title = request.title,
            description = request.description,
            changeType = request.changeType,
            geometry = request.geometry,
            createdBy = request.createdBy,
            metadata = request.metadata
        )
        
        val saved = changesetRepository.save(changeset)
        
        // 캐시 무효화
        invalidateCache()
        
        return saved.toDto()
    }
    
    /**
     * 타입별 통계 조회
     */
    fun getStatistics(): Map<ChangeType, Long> {
        val since = java.time.LocalDateTime.now().minusDays(30)
        val results = changesetRepository.countByTypesSince(since)
        
        return results.associate { row ->
            row[0] as ChangeType to row[1] as Long
        }
    }
    
    /**
     * 캐시 무효화
     */
    private fun invalidateCache() {
        try {
            // 로컬 캐시 무효화
            cacheManager.evictLocal(CACHE_NAME)
            
            // 분산 무효화 (Redis Pub/Sub)
            cacheManager.evictDistributed(CACHE_NAME)
            
            logger.info { "Changeset cache invalidated" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to invalidate changeset cache" }
        }
    }
}

/**
 * 변경사항 DTO
 */
data class ChangesetDto(
    val id: Long,
    val title: String,
    val description: String?,
    val changeType: ChangeType,
    val geometryWkt: String?,
    val createdAt: java.time.LocalDateTime,
    val createdBy: String,
    val metadata: String?
)

/**
 * 변경사항 생성 요청
 */
data class CreateChangesetRequest(
    val title: String,
    val description: String? = null,
    val changeType: ChangeType,
    val geometry: org.locationtech.jts.geom.Polygon? = null,
    val createdBy: String,
    val metadata: String? = null
)

/**
 * Entity to DTO 변환
 */
private fun Changeset.toDto(): ChangesetDto {
    return ChangesetDto(
        id = id,
        title = title,
        description = description,
        changeType = changeType,
        geometryWkt = geometry?.let { com.template.platform.common.geo.GeoUtils.toWKT(it) },
        createdAt = createdAt,
        createdBy = createdBy,
        metadata = metadata
    )
}