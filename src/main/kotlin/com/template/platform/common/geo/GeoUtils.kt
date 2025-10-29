package com.template.platform.common.geo

import org.locationtech.jts.geom.*
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter
import java.security.MessageDigest

/**
 * GIS 유틸리티
 * 
 * JTS 기반 BBOX, SRID 변환, 캐시 키 생성 등 지원
 */
object GeoUtils {
    
    private val geometryFactory = GeometryFactory()
    private val wktReader = WKTReader(geometryFactory)
    private val wktWriter = WKTWriter()
    
    /**
     * BBOX(Bounding Box)를 JTS Envelope로 변환
     */
    fun createBBox(minX: Double, minY: Double, maxX: Double, maxY: Double): Envelope {
        return Envelope(minX, maxX, minY, maxY)
    }
    
    /**
     * BBOX를 Polygon으로 변환
     */
    fun bboxToPolygon(minX: Double, minY: Double, maxX: Double, maxY: Double): Polygon {
        val coordinates = arrayOf(
            Coordinate(minX, minY),
            Coordinate(maxX, minY),
            Coordinate(maxX, maxY),
            Coordinate(minX, maxY),
            Coordinate(minX, minY) // 닫힌 고리
        )
        
        val linearRing = geometryFactory.createLinearRing(coordinates)
        return geometryFactory.createPolygon(linearRing)
    }
    
    /**
     * WKT 문자열을 Geometry로 변환
     */
    fun parseWKT(wkt: String): Geometry {
        return try {
            wktReader.read(wkt)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid WKT format: $wkt", e)
        }
    }
    
    /**
     * Geometry를 WKT 문자열로 변환
     */
    fun toWKT(geometry: Geometry): String {
        return wktWriter.write(geometry)
    }
    
    /**
     * BBOX 기반 캐시 키 생성
     * 
     * 좌표를 정규화하고 해시하여 일관된 캐시 키 생성
     */
    fun generateCacheKey(minX: Double, minY: Double, maxX: Double, maxY: Double, prefix: String = "geo"): String {
        // 소수점 6자리로 정규화 (약 0.1m 정밀도)
        val normalizedCoords = listOf(minX, minY, maxX, maxY)
            .map { "%.6f".format(it) }
            .joinToString(",")
        
        return "$prefix:${normalizedCoords.md5()}"
    }
    
    /**
     * 두 Geometry 간의 교차 여부 확인
     */
    fun intersects(geom1: Geometry, geom2: Geometry): Boolean {
        return try {
            geom1.intersects(geom2)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Geometry의 면적 계산 (평면 좌표계 기준)
     */
    fun calculateArea(geometry: Geometry): Double {
        return geometry.area
    }
    
    /**
     * 좌표계 정보 추출 (WKT에서 SRID 파싱)
     */
    fun extractSRID(wkt: String): Int? {
        val sridRegex = """SRID=(\d+);""".toRegex()
        return sridRegex.find(wkt)?.groupValues?.get(1)?.toIntOrNull()
    }
    
    /**
     * SRID 정보 확인
     */
    fun isValidSRID(srid: Int): Boolean {
        return srid in 1000..32767 // 일반적인 EPSG 코드 범위
    }
    
    /**
     * 기본적인 좌표 검증
     */
    fun isValidCoordinate(longitude: Double, latitude: Double): Boolean {
        return longitude in -180.0..180.0 && latitude in -90.0..90.0
    }
    
    /**
     * BBOX 검증
     */
    fun isValidBBox(minX: Double, minY: Double, maxX: Double, maxY: Double): Boolean {
        return minX < maxX && minY < maxY && 
               isValidCoordinate(minX, minY) && 
               isValidCoordinate(maxX, maxY)
    }
}

/**
 * MD5 해시 생성 확장 함수
 */
private fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

/**
 * BBOX 데이터 클래스
 */
data class BBox(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
) {
    init {
        require(GeoUtils.isValidBBox(minX, minY, maxX, maxY)) {
            "Invalid BBOX coordinates: ($minX, $minY, $maxX, $maxY)"
        }
    }
    
    fun toEnvelope(): Envelope = GeoUtils.createBBox(minX, minY, maxX, maxY)
    fun toPolygon(): Polygon = GeoUtils.bboxToPolygon(minX, minY, maxX, maxY)
    fun toCacheKey(prefix: String = "geo"): String = GeoUtils.generateCacheKey(minX, minY, maxX, maxY, prefix)
}