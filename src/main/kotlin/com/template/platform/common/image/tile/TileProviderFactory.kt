package com.template.platform.common.image.tile

import mu.KotlinLogging
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap

/**
 * 타일 제공자 팩토리 및 매니저
 * 
 * 다양한 타일 제공자를 관리하고 타일 캐싱 기능을 제공
 * - 제공자 등록/선택
 * - 메모리 캐싱
 * - 폴백 제공자 지원
 * 
 * BMOA 프로젝트에서 이전 및 확장됨
 */
object TileProviderFactory {
    
    private val logger = KotlinLogging.logger {}
    
    // 등록된 타일 제공자들
    private val providers = mutableMapOf<String, TileProvider>()
    
    // 타일 캐시 (메모리)
    private val tileCache = ConcurrentHashMap<String, BufferedImage>()
    
    // 캐시 최대 크기 (개수)
    private var maxCacheSize = 1000
    
    init {
        // 기본 제공자들 등록
        registerProvider("arcgis", ArcGisTileProvider())
        registerProvider("osm", OpenStreetMapTileProvider())
    }
    
    /**
     * 타일 제공자 등록
     */
    fun registerProvider(name: String, provider: TileProvider) {
        providers[name] = provider
        logger.info { "타일 제공자 등록: $name - ${provider.providerName}" }
    }
    
    /**
     * 등록된 제공자 목록 조회
     */
    fun getRegisteredProviders(): Map<String, TileProvider> = providers.toMap()
    
    /**
     * 제공자별 설명 조회
     */
    fun getProviderInfo(): Map<String, String> {
        return providers.mapValues { (_, provider) -> 
            "${provider.providerName}: ${provider.description}"
        }
    }
    
    /**
     * 특정 제공자로 타일 가져오기 (캐싱 지원)
     */
    fun getTile(providerName: String, x: Int, y: Int, z: Int, useCache: Boolean = true): BufferedImage? {
        val provider = providers[providerName] ?: run {
            logger.warn { "등록되지 않은 타일 제공자: $providerName" }
            return null
        }
        
        val cacheKey = if (useCache) "$providerName:$z:$x:$y" else null
        
        // 캐시에서 먼저 확인
        if (useCache && cacheKey != null) {
            tileCache[cacheKey]?.let { cachedTile ->
                logger.debug { "캐시에서 타일 반환: $cacheKey" }
                return cachedTile
            }
        }
        
        // 제공자에서 타일 가져오기
        val tile = provider.getTile(x, y, z)
        
        // 캐시에 저장
        if (useCache && tile != null && cacheKey != null) {
            // 캐시 크기 제한
            if (tileCache.size >= maxCacheSize) {
                cleanCache()
            }
            tileCache[cacheKey] = tile
            logger.debug { "타일 캐시에 저장: $cacheKey" }
        }
        
        return tile
    }
    
    /**
     * 폴백을 지원하는 타일 가져오기
     */
    fun getTileWithFallback(
        primaryProvider: String,
        fallbackProviders: List<String>,
        x: Int, y: Int, z: Int,
        useCache: Boolean = true
    ): BufferedImage? {
        // 1차 제공자 시도
        getTile(primaryProvider, x, y, z, useCache)?.let { return it }
        
        // 폴백 제공자들 순서대로 시도
        for (fallbackProvider in fallbackProviders) {
            getTile(fallbackProvider, x, y, z, useCache)?.let { 
                logger.debug { "폴백 제공자로 타일 획득: $fallbackProvider" }
                return it 
            }
        }
        
        logger.warn { "모든 제공자에서 타일 획득 실패: z=$z, x=$x, y=$y" }
        return null
    }
    
    /**
     * ArcGIS 타일 가져오기 (편의 함수)
     */
    fun getArcGisTile(x: Int, y: Int, z: Int, useCache: Boolean = true): BufferedImage? {
        return getTile("arcgis", x, y, z, useCache)
    }
    
    /**
     * OpenStreetMap 타일 가져오기 (편의 함수)
     */
    fun getOsmTile(x: Int, y: Int, z: Int, useCache: Boolean = true): BufferedImage? {
        return getTile("osm", x, y, z, useCache)
    }
    
    /**
     * 배치로 여러 타일 가져오기
     */
    fun getTiles(
        providerName: String,
        tiles: List<Triple<Int, Int, Int>>,
        useCache: Boolean = true
    ): Map<Triple<Int, Int, Int>, BufferedImage?> {
        return tiles.associateWith { (x, y, z) -> getTile(providerName, x, y, z, useCache) }
    }
    
    /**
     * 캐시 정리 (LRU 방식으로 절반 제거)
     */
    private fun cleanCache() {
        val entriesToRemove = tileCache.size / 2
        val keysToRemove = tileCache.keys.take(entriesToRemove)
        keysToRemove.forEach { tileCache.remove(it) }
        logger.debug { "타일 캐시 정리: ${keysToRemove.size}개 항목 제거" }
    }
    
    /**
     * 캐시 완전 정리
     */
    fun clearCache() {
        val removedCount = tileCache.size
        tileCache.clear()
        logger.info { "타일 캐시 완전 정리: ${removedCount}개 항목 제거" }
    }
    
    /**
     * 캐시 상태 조회
     */
    fun getCacheStats(): Map<String, Any> {
        return mapOf(
            "size" to tileCache.size,
            "maxSize" to maxCacheSize,
            "memoryUsageEstimate" to "${tileCache.size * 256 * 256 * 4 / 1024 / 1024}MB" // 대략적인 메모리 사용량
        )
    }
    
    /**
     * 캐시 최대 크기 설정
     */
    fun setMaxCacheSize(size: Int) {
        maxCacheSize = size
        if (tileCache.size > maxCacheSize) {
            cleanCache()
        }
        logger.info { "타일 캐시 최대 크기 설정: $maxCacheSize" }
    }
}