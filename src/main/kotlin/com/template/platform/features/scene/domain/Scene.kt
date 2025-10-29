package com.template.platform.features.scene.domain

import com.template.platform.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Polygon
import java.sql.Types
import java.time.Instant

@Entity
@Table(name = "scenes")
@SQLRestriction("deleted_at IS NULL")
class Scene(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val sceneId: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(name = "image_created_at", nullable = false)
    var imageCreatedAt: Instant,

    @Column(nullable = false)
    var province: String,

    @Column(nullable = false)
    var district: String,

    @Column(name = "total_size", nullable = false)
    var totalSize: Long,

    @Column(name = "cog_file_path")
    var cogFilePath: String? = null,

    @JdbcTypeCode(Types.ARRAY)
    @Column(name = "geotransform", columnDefinition = "double precision[]")
    var geotransform: Array<Double>? = null,

    @Column
    var projection: String? = null,

    @Column(name = "geodetic_polygon", columnDefinition = "geometry(Polygon, 4326)")
    var geodeticPolygon: Polygon? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var status: SceneStatus = SceneStatus.PENDING,

    @Column
    var gsd: Double? = null,

    @Column(name = "stac_url")
    var stacUrl: String? = null
) : BaseEntity() {

    fun updateFrom(
        name: String,
        imageCreatedAt: Instant,
        province: String,
        district: String,
        totalSize: Long,
        cogFilePath: String?,
        geotransform: Array<Double>?,
        projection: String?,
        geodeticPolygon: Polygon?,
        status: SceneStatus,
        gsd: Double?,
        stacUrl: String?
    ) {
        this.name = name
        this.imageCreatedAt = imageCreatedAt
        this.province = province
        this.district = district
        this.totalSize = totalSize
        this.cogFilePath = cogFilePath
        this.geotransform = geotransform
        this.projection = projection
        this.geodeticPolygon = geodeticPolygon
        this.status = status
        this.gsd = gsd
        this.stacUrl = stacUrl
    }
}

enum class SceneStatus {
    PENDING,
    COMPLETED,
    FAILED,
    UNKNOWN
}
