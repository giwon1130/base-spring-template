package com.template.platform.common.cache

import org.springframework.context.annotation.Configuration

/**
 * 캐시 이름 상수 정의.
 *
 * BMOA에서 사용하던 캐시 키를 그대로 유지해
 * 마이그레이션 중 기능 호환성을 보장한다.
 */
@Configuration
class CacheConfig {
    companion object {
        const val CHANGE_DETECTION_DETAIL = "changeDetectionDetail"
        const val CHANGE_DETECTION_SUMMARY = "changeDetectionSummary"
        const val AOI_GEOMETRY = "aoiGeometry"
        const val CHANGE_DETECTION_RESULTS = "changeDetectionResults"
    }
}
