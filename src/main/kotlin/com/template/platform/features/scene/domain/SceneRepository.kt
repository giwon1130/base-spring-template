package com.template.platform.features.scene.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface SceneRepository : JpaRepository<Scene, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
    fun countByCreatedAtBetweenAndDeletedAtIsNull(start: Instant, end: Instant): Long
}
