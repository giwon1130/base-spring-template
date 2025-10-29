package com.template.platform.common.image.tile

import mu.KotlinLogging
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

/**
 * OpenStreetMap 타일 제공자
 * 
 * OpenStreetMap 표준 타일 서비스에서 지도 타일을 가져옴
 * - 무료 오픈소스 지도 서비스
 * - 전 세계 커버리지
 * - 사용 정책 준수 필요 (User-Agent 필수)
 * 
 * BMOA 프로젝트에서 이전됨
 */
class OpenStreetMapTileProvider : TileProvider {
    
    private val logger = KotlinLogging.logger {}
    
    override val providerName = "OpenStreetMap"
    override val description = "OpenStreetMap 표준 지도 타일"
    override val maxZoomLevel = 19
    override val urlPattern = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
    
    override fun getTile(x: Int, y: Int, z: Int): BufferedImage? {
        // 줌 레벨 유효성 검사
        if (z < minZoomLevel || z > maxZoomLevel) {
            logger.warn { "줌 레벨 범위 초과: z=$z (지원 범위: $minZoomLevel-$maxZoomLevel)" }
            return null
        }
        
        val urlStr = "https://tile.openstreetmap.org/$z/$x/$y.png"
        
        return try {
            val connection = URL(urlStr).openConnection().apply {
                connectTimeout = 3000  // 3초 연결 타임아웃
                readTimeout = 5000     // 5초 읽기 타임아웃
                // OSM 사용 정책에 따라 User-Agent 필수 설정
                setRequestProperty("User-Agent", "Platform-Template/1.0 (platform@template.com)")
                setRequestProperty("Referer", "https://platform-template.com")
            }
            
            val image = ImageIO.read(connection.getInputStream())
            
            if (image != null) {
                logger.debug { "OSM 타일 로드 성공: z=$z, x=$x, y=$y" }
            } else {
                logger.warn { "OSM 타일 이미지가 null: z=$z, x=$x, y=$y" }
            }
            
            image
        } catch (e: Exception) {
            logger.error(e) { "OSM 타일 로드 실패: z=$z, x=$x, y=$y" }
            null
        }
    }
    
    /**
     * 타일 URL 생성 (캐싱이나 프리로딩 시 사용)
     */
    fun getTileUrl(x: Int, y: Int, z: Int): String {
        return "https://tile.openstreetmap.org/$z/$x/$y.png"
    }
    
    /**
     * 배치로 여러 타일을 가져오는 함수
     */
    fun getTiles(tiles: List<Triple<Int, Int, Int>>): Map<Triple<Int, Int, Int>, BufferedImage?> {
        return tiles.associateWith { (x, y, z) -> getTile(x, y, z) }
    }
    
    companion object {
        /**
         * OSM 사용 정책
         * - 적절한 User-Agent 설정 필수
         * - 과도한 요청 방지 (rate limiting)
         * - 캐싱 권장
         * - 상업적 사용 시 기여 고려
         */
        const val USAGE_POLICY_URL = "https://operations.osmfoundation.org/policies/tiles/"
    }
}