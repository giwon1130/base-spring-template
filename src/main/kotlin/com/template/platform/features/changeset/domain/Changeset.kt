package com.template.platform.features.changeset.domain

import jakarta.persistence.*
import org.locationtech.jts.geom.Polygon
import java.time.LocalDateTime

/**
 * 변경사항 도메인 엔티티
 */
@Entity
@Table(
    name = "changesets",
    indexes = [
        Index(name = "idx_changeset_created_at", columnList = "createdAt"),
        Index(name = "idx_changeset_type", columnList = "changeType")
    ]
)
class Changeset(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, length = 100)
    val title: String,
    
    @Column(length = 500)
    val description: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val changeType: ChangeType,
    
    @Column(columnDefinition = "geometry(Polygon, 4326)")
    val geometry: Polygon? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val createdBy: String,
    
    @Column
    val metadata: String? = null // JSON 형태의 추가 메타데이터
)

/**
 * 변경사항 타입
 */
enum class ChangeType {
    FEATURE_ADDED,      // 새 기능 추가
    FEATURE_MODIFIED,   // 기능 변경
    FEATURE_REMOVED,    // 기능 제거
    DATA_UPDATED,       // 데이터 업데이트
    CONFIGURATION,      // 설정 변경
    OTHER              // 기타
}