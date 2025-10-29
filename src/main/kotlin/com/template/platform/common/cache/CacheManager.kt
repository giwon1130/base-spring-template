package com.template.platform.common.cache

import mu.KotlinLogging
import org.springframework.cache.Cache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

/**
 * 캐시 관리자
 * 
 * Spring Cache와 Redis Pub/Sub을 연동한 분산 캐시 무효화
 */
@Component("platformCacheManager")
class CacheManager(
    private val springCacheManager: org.springframework.cache.CacheManager,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = KotlinLogging.logger {}
    
    companion object {
        const val CACHE_INVALIDATION_CHANNEL = "platform.cache.invalidation"
    }
    
    /**
     * 로컬 캐시 무효화
     */
    fun evictLocal(cacheName: String, key: String? = null) {
        val cache = springCacheManager.getCache(cacheName)
        if (cache != null) {
            if (key != null) {
                cache.evict(key)
                logger.debug { "Evicted cache: $cacheName, key: $key" }
            } else {
                cache.clear()
                logger.debug { "Cleared cache: $cacheName" }
            }
        }
    }
    
    /**
     * 분산 캐시 무효화 (Redis Pub/Sub)
     */
    fun evictDistributed(cacheName: String, key: String? = null) {
        val invalidationMessage = CacheInvalidationMessage(
            cacheName = cacheName,
            key = key
        )
        
        try {
            redisTemplate.convertAndSend(
                CACHE_INVALIDATION_CHANNEL, 
                invalidationMessage.toJson()
            )
            logger.info { "Published cache invalidation: $cacheName${if (key != null) ":$key" else ""}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to publish cache invalidation" }
        }
    }
    
    /**
     * 캐시 통계 정보 조회
     */
    fun getCacheNames(): Collection<String> {
        return springCacheManager.cacheNames
    }
    
    /**
     * 특정 캐시 정보 조회
     */
    fun getCacheInfo(cacheName: String): Map<String, Any?> {
        val cache = springCacheManager.getCache(cacheName)
        return mapOf(
            "name" to cacheName,
            "exists" to (cache != null),
            "nativeCache" to cache?.nativeCache?.javaClass?.simpleName
        )
    }
}
