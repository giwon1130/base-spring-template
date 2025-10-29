package com.template.platform.features.aoi.domain

import com.template.platform.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.locationtech.jts.geom.Polygon

@Entity
@Table(name = "aois")
@SQLRestriction("deleted_at IS NULL")
class Aoi(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val aoiId: Long = 0,

    @Column(columnDefinition = "geometry(Polygon, 4326)", nullable = false)
    var geometry: Polygon,

    @Column(name = "code_name", length = 30, nullable = false, unique = true)
    var codeName: String
) : BaseEntity() {

    fun updateFrom(
        geometry: Polygon,
        codeName: String
    ) {
        this.geometry = geometry
        this.codeName = codeName
    }
}
