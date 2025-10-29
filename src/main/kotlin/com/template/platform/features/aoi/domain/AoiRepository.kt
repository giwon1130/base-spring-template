package com.template.platform.features.aoi.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface AoiRepository : JpaRepository<Aoi, Long>, JpaSpecificationExecutor<Aoi> {
    fun existsByCodeNameIgnoreCase(codeName: String): Boolean
}
