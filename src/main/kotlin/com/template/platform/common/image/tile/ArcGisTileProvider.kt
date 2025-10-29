package com.template.platform.common.image.tile

import mu.KotlinLogging
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

/**
 * ArcGIS 위성영상 타일 제공자
 * 
 * ArcGIS World Imagery 서비스에서 위성영상 타일을 가져옴
 * - 고해상도 위성영상 제공
 * - 전 세계 커버리지
 * - 상업적 사용 시 라이선스 확인 필요
 * 
 * BMOA 프로젝트에서 이전됨
 */
class ArcGisTileProvider : TileProvider {
    
    private val logger = KotlinLogging.logger {}
    
    override val providerName = "ArcGIS World Imagery"
    override val description = "ArcGIS 고해상도 위성영상 타일"
    override val maxZoomLevel = 19
    override val urlPattern = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
    
    override fun getTile(x: Int, y: Int, z: Int): BufferedImage? {
        // 줌 레벨 유효성 검사
        if (z < minZoomLevel || z > maxZoomLevel) {
            logger.warn { "줌 레벨 범위 초과: z=$z (지원 범위: $minZoomLevel-$maxZoomLevel)" }
            return null
        }
        
        val urlStr = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$z/$y/$x"
        
        return try {
            val connection = URL(urlStr).openConnection().apply {
                connectTimeout = 3000  // 3초 연결 타임아웃
                readTimeout = 5000     // 5초 읽기 타임아웃
                setRequestProperty("User-Agent", "Platform-Template/1.0")
                setRequestProperty("Referer", "https://platform-template.com")
            }
            
            val image = ImageIO.read(connection.getInputStream())
            
            if (image != null) {
                logger.debug { "ArcGIS 타일 로드 성공: z=$z, x=$x, y=$y" }
            } else {
                logger.warn { "ArcGIS 타일 이미지가 null: z=$z, x=$x, y=$y" }
            }
            
            image
        } catch (e: Exception) {
            logger.error(e) { "ArcGIS 타일 로드 실패: z=$z, x=$x, y=$y" }
            null
        }
    }
    
    /**
     * 타일 URL 생성 (캐싱이나 프리로딩 시 사용)
     */
    fun getTileUrl(x: Int, y: Int, z: Int): String {
        return "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$z/$y/$x"
    }
    
    /**
     * 배치로 여러 타일을 가져오는 함수
     */
    fun getTiles(tiles: List<Triple<Int, Int, Int>>): Map<Triple<Int, Int, Int>, BufferedImage?> {
        return tiles.associateWith { (x, y, z) -> getTile(x, y, z) }
    }
}