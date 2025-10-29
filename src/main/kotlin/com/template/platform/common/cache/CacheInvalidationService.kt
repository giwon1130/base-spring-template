package com.template.platform.common.cache

import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * 캐시 무효화 및 현황 조회 서비스.
 *
 * Redis Pub/Sub을 이용해 분산 환경에서도 일관된 캐시 무효화를 수행한다.
 */
@Service
class CacheInvalidationService(
    private val cacheManager: CacheManager
) {

    private val logger = KotlinLogging.logger {}

    fun evictChangeDetectionDetail(changeDetectionId: Long) {
        evict(cacheName = CacheConfig.CHANGE_DETECTION_DETAIL, key = changeDetectionId.toString())
    }

    fun evictChangeDetectionSummary(targetDate: LocalDate) {
        evict(cacheName = CacheConfig.CHANGE_DETECTION_SUMMARY, key = targetDate.toString())
    }

    fun evictAoiGeometry(quadId: String) {
        evict(cacheName = CacheConfig.AOI_GEOMETRY, key = quadId)
    }

    fun evictAllChangeDetectionCaches() {
        listOf(
            CacheConfig.CHANGE_DETECTION_DETAIL,
            CacheConfig.CHANGE_DETECTION_SUMMARY,
            CacheConfig.CHANGE_DETECTION_RESULTS
        ).forEach { evict(it, null) }
    }

    fun evictCache(cacheName: String, key: String?) {
        evict(cacheName, key)
    }

    fun getCacheStats(): Map<String, Any> {
        return cacheManager.getCacheNames().associateWith { cacheName ->
            cacheManager.getCacheInfo(cacheName)
        }
    }

    private fun evict(cacheName: String, key: String?) {
        try {
            cacheManager.evictLocal(cacheName, key)
            cacheManager.evictDistributed(cacheName, key)
            logger.info { "캐시 무효화 완료: $cacheName${key?.let { ":$it" } ?: ""}" }
        } catch (e: Exception) {
            logger.warn(e) { "캐시 무효화 실패: $cacheName${key?.let { ":$it" } ?: ""}" }
        }
    }
}
