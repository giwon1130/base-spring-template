package com.template.platform.features.changeset.infrastructure

import com.template.platform.features.changeset.domain.Changeset
import com.template.platform.features.changeset.domain.ChangeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

/**
 * 변경사항 리포지토리
 */
interface ChangesetRepository : JpaRepository<Changeset, Long> {
    
    /**
     * BBOX 영역 내의 변경사항 조회
     */
    @Query("""
        SELECT c FROM Changeset c 
        WHERE ST_Intersects(c.geometry, ST_MakeEnvelope(:minX, :minY, :maxX, :maxY, 4326)) = true
        ORDER BY c.createdAt DESC
    """)
    fun findByBBox(
        @Param("minX") minX: Double,
        @Param("minY") minY: Double,
        @Param("maxX") maxX: Double,
        @Param("maxY") maxY: Double
    ): List<Changeset>
    
    /**
     * 변경 타입별 조회
     */
    fun findByChangeTypeOrderByCreatedAtDesc(changeType: ChangeType): List<Changeset>
    
    /**
     * 생성자별 조회
     */
    fun findByCreatedByOrderByCreatedAtDesc(createdBy: String): List<Changeset>
    
    /**
     * 기간별 조회
     */
    fun findByCreatedAtBetweenOrderByCreatedAtDesc(
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Changeset>
    
    /**
     * 통계 조회: 타입별 카운트
     */
    @Query("""
        SELECT c.changeType, COUNT(c) 
        FROM Changeset c 
        WHERE c.createdAt >= :since
        GROUP BY c.changeType
    """)
    fun countByTypesSince(@Param("since") since: LocalDateTime): List<Array<Any>>
}