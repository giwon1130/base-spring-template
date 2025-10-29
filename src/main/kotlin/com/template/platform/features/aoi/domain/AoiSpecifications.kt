package com.template.platform.features.aoi.domain

import org.springframework.data.jpa.domain.Specification

object AoiSpecifications {
    fun keywordLike(keyword: String?): Specification<Aoi>? {
        if (keyword.isNullOrBlank()) return null
        val like = "%${keyword.trim()}%".lowercase()
        return Specification { root, _, cb ->
            cb.like(cb.lower(root.get("codeName")), like)
        }
    }
}
