package com.template.platform.common.image

import kotlin.math.*

/**
 * 지도 타일 좌표 변환을 위한 유틸리티 클래스
 * 
 * 주요 기능:
 * - 위경도 좌표 ↔ 타일 좌표 변환
 * - 위경도 좌표 ↔ 픽셀 좌표 변환  
 * - 타일 좌표 ↔ 픽셀 좌표 변환
 * 
 * 웹 메르카토르 투영법(EPSG:3857) 기반으로 동작
 * OpenStreetMap, Google Maps, ArcGIS 등 표준 타일 서비스와 호환
 * 
 * BMOA 프로젝트에서 이전됨
 */
object MapTileUtils {
    
    /**
     * 위경도 좌표를 타일 XY 좌표로 변환
     * 
     * @param lon 경도 (-180 ~ 180)
     * @param lat 위도 (-85.0511 ~ 85.0511)
     * @param zoom 줌 레벨 (0 ~ 18)
     * @return 타일 좌표 (x, y)
     */
    fun lonLatToTileXY(lon: Double, lat: Double, zoom: Int): Pair<Int, Int> {
        val x = ((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()
        val latRad = Math.toRadians(lat)
        val y = ((1.0 - ln(tan(latRad) + 1 / cos(latRad)) / PI) / 2.0 * (1 shl zoom)).toInt()
        return x to y
    }

    /**
     * 경도를 전체 픽셀 X 좌표로 변환
     * 
     * @param lon 경도
     * @param zoom 줌 레벨
     * @param tileSize 타일 크기 (기본 256px)
     * @return 픽셀 X 좌표
     */
    fun lonToPixelX(lon: Double, zoom: Int, tileSize: Int = 256): Int {
        val scale = 1 shl zoom
        return ((lon + 180.0) / 360.0 * scale * tileSize).toInt()
    }

    /**
     * 위도를 전체 픽셀 Y 좌표로 변환
     * 
     * @param lat 위도
     * @param zoom 줌 레벨
     * @param tileSize 타일 크기 (기본 256px)
     * @return 픽셀 Y 좌표
     */
    fun latToPixelY(lat: Double, zoom: Int, tileSize: Int = 256): Int {
        val scale = 1 shl zoom
        val latRad = Math.toRadians(lat)
        return (((1.0 - ln(tan(latRad) + 1 / cos(latRad)) / PI) / 2.0) * scale * tileSize).toInt()
    }

    /**
     * 타일 X 좌표를 픽셀 X 좌표로 변환
     * 
     * @param tileX 타일 X 좌표
     * @param tileSize 타일 크기 (기본 256px)
     * @return 픽셀 X 좌표
     */
    fun tileXToPixelX(tileX: Int, tileSize: Int = 256): Int {
        return tileX * tileSize
    }

    /**
     * 타일 Y 좌표를 픽셀 Y 좌표로 변환
     * 
     * @param tileY 타일 Y 좌표
     * @param tileSize 타일 크기 (기본 256px)
     * @return 픽셀 Y 좌표
     */
    fun tileYToPixelY(tileY: Int, tileSize: Int = 256): Int {
        return tileY * tileSize
    }
    
    /**
     * 픽셀 X 좌표를 경도로 변환
     * 
     * @param pixelX 픽셀 X 좌표
     * @param zoom 줌 레벨
     * @param tileSize 타일 크기 (기본 256px)
     * @return 경도
     */
    fun pixelXToLon(pixelX: Int, zoom: Int, tileSize: Int = 256): Double {
        val scale = 1 shl zoom
        return (pixelX.toDouble() / (scale * tileSize)) * 360.0 - 180.0
    }
    
    /**
     * 픽셀 Y 좌표를 위도로 변환
     * 
     * @param pixelY 픽셀 Y 좌표
     * @param zoom 줌 레벨
     * @param tileSize 타일 크기 (기본 256px)
     * @return 위도
     */
    fun pixelYToLat(pixelY: Int, zoom: Int, tileSize: Int = 256): Double {
        val scale = 1 shl zoom
        val n = PI - 2.0 * PI * pixelY / (scale * tileSize)
        return Math.toDegrees(atan(sinh(n)))
    }
    
    /**
     * 두 좌표 간의 거리 계산 (미터)
     * Haversine 공식 사용
     * 
     * @param lon1 첫 번째 지점 경도
     * @param lat1 첫 번째 지점 위도
     * @param lon2 두 번째 지점 경도
     * @param lat2 두 번째 지점 위도
     * @return 거리 (미터)
     */
    fun calculateDistance(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double {
        val earthRadius = 6371000.0 // 지구 반지름 (미터)
        
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * 줌 레벨에서의 1픽셀당 실제 거리 계산 (미터/픽셀)
     * 
     * @param lat 위도 (거리는 위도에 따라 변함)
     * @param zoom 줌 레벨
     * @param tileSize 타일 크기 (기본 256px)
     * @return 미터/픽셀
     */
    fun metersPerPixel(lat: Double, zoom: Int, tileSize: Int = 256): Double {
        val earthCircumference = 40075016.686 // 지구 둘레 (미터)
        val latRad = Math.toRadians(lat)
        return earthCircumference * cos(latRad) / ((1 shl zoom) * tileSize)
    }
}