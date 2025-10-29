package com.template.platform.common.util

import mu.KotlinLogging
import org.locationtech.jts.geom.*
import org.locationtech.jts.io.ParseException
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter

/**
 * JTS Geometry 통합 유틸리티
 * 
 * 주요 기능:
 * - WKT ↔ Geometry 변환
 * - BBOX 처리 (생성, 변환, 검증)
 * - Polygon 생성 및 검증
 * - 좌표계 검증 (SRID, 좌표 범위)
 * - 안전한 링 처리 (닫힌 링 보장)
 * - 캐시 키 생성 (좌표 기반)
 * - Geometry 유효성 검증 및 면적 계산
 */
object GeometryUtils {
    private val logger = KotlinLogging.logger {}
    private val geometryFactory = GeometryFactory()
    private val wktReader = WKTReader(geometryFactory)
    private val wktWriter = WKTWriter()

    // ===== WKT ↔ Geometry 변환 =====
    
    /**
     * WKT 문자열을 Polygon으로 변환
     * 
     * @param wkt WKT 형식의 문자열
     * @return Polygon 객체 (SRID 4326 설정됨) 또는 null
     * @throws IllegalArgumentException WKT 파싱 실패 시
     */
    fun polygonFromWkt(wkt: String?): Polygon? {
        if (wkt.isNullOrBlank()) return null
        
        return try {
            val geometry = wktReader.read(wkt)
            require(geometry is Polygon) { "유효한 Polygon WKT 형식이 아닙니다." }
            geometry.srid = 4326
            geometry
        } catch (ex: ParseException) {
            logger.warn { "WKT 파싱 실패: $wkt" }
            throw IllegalArgumentException("잘못된 WKT 형식입니다: ${ex.message}", ex)
        }
    }

    /**
     * WKT 문자열을 Geometry로 변환 (모든 타입 지원)
     * 
     * @param wkt WKT 형식의 문자열
     * @return Geometry 객체 (SRID 4326 설정됨)
     * @throws IllegalArgumentException WKT 파싱 실패 시
     */
    fun parseWktToGeometry(wkt: String): Geometry {
        return try {
            val geometry = wktReader.read(wkt)
            geometry.srid = 4326
            geometry
        } catch (e: Exception) {
            logger.warn { "⚠️ WKT 파싱 실패: $wkt" }
            throw IllegalArgumentException("WKT 파싱에 실패했습니다: ${e.message}", e)
        }
    }

    /**
     * Polygon을 WKT 문자열로 변환
     * 
     * @param polygon Polygon 객체
     * @return WKT 문자열 또는 null
     */
    fun toWkt(polygon: Polygon?): String? {
        return polygon?.let { wktWriter.write(it) }
    }

    /**
     * Geometry를 WKT 문자열로 변환
     * 
     * @param geometry Geometry 객체
     * @return WKT 문자열 또는 null
     */
    fun toWkt(geometry: Geometry?): String? {
        return geometry?.let { wktWriter.write(it) }
    }

    // ===== BBOX 처리 =====
    
    /**
     * BBOX(Bounding Box)를 JTS Envelope로 변환
     * 
     * @param minX 최소 X 좌표
     * @param minY 최소 Y 좌표  
     * @param maxX 최대 X 좌표
     * @param maxY 최대 Y 좌표
     * @return Envelope 객체
     */
    fun createBBox(minX: Double, minY: Double, maxX: Double, maxY: Double): Envelope {
        return Envelope(minX, maxX, minY, maxY)
    }
    
    /**
     * BBOX를 Polygon으로 변환
     * 
     * @param minX 최소 X 좌표
     * @param minY 최소 Y 좌표
     * @param maxX 최대 X 좌표
     * @param maxY 최대 Y 좌표
     * @return Polygon 객체 (SRID 4326)
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
        val polygon = geometryFactory.createPolygon(linearRing)
        polygon.srid = 4326
        return polygon
    }

    // ===== Polygon 생성 및 처리 =====

    /**
     * 좌표 배열을 Polygon으로 변환
     * 
     * @param coordinates 좌표 배열 (외부 링)
     * @return Polygon 객체 (SRID 4326)
     */
    fun coordinatesToPolygon(coordinates: List<Coordinate>): Polygon {
        val coordArray = coordinates.toTypedArray()
        val exteriorRing = geometryFactory.createLinearRing(coordArray)
        val polygon = geometryFactory.createPolygon(exteriorRing, null)
        polygon.srid = 4326
        return polygon
    }

    /**
     * 닫힌 링 보장 (첫 좌표와 마지막 좌표가 같도록)
     * 
     * @param coords 좌표 배열
     * @return 닫힌 좌표 배열
     */
    fun ensureClosedRing(coords: Array<Coordinate>): Array<Coordinate> {
        return if (coords.isNotEmpty() && !coords.first().equals2D(coords.last())) {
            coords + coords.first()
        } else {
            coords
        }
    }

    /**
     * Point 생성
     * 
     * @param x 경도
     * @param y 위도
     * @return Point 객체 (SRID 4326)
     */
    fun createPoint(x: Double, y: Double): Point {
        val point = geometryFactory.createPoint(Coordinate(x, y))
        point.srid = 4326
        return point
    }

    // ===== 검증 및 계산 =====

    /**
     * Geometry가 유효한지 검증
     * 
     * @param geometry 검증할 Geometry
     * @return 유효하면 true, 아니면 false
     */
    fun isValidGeometry(geometry: Geometry?): Boolean {
        return geometry?.isValid == true
    }

    /**
     * 기본적인 좌표 검증
     * 
     * @param longitude 경도
     * @param latitude 위도
     * @return 유효한 좌표이면 true
     */
    fun isValidCoordinate(longitude: Double, latitude: Double): Boolean {
        return longitude in -180.0..180.0 && latitude in -90.0..90.0
    }
    
    /**
     * BBOX 검증
     * 
     * @param minX 최소 X 좌표
     * @param minY 최소 Y 좌표
     * @param maxX 최대 X 좌표
     * @param maxY 최대 Y 좌표
     * @return 유효한 BBOX이면 true
     */
    fun isValidBBox(minX: Double, minY: Double, maxX: Double, maxY: Double): Boolean {
        return minX < maxX && minY < maxY && 
               isValidCoordinate(minX, minY) && 
               isValidCoordinate(maxX, maxY)
    }

    /**
     * Polygon의 면적 계산 (평방미터 단위)
     * 
     * @param polygon Polygon 객체
     * @return 면적 (평방미터)
     */
    fun calculateArea(polygon: Polygon?): Double {
        return polygon?.area ?: 0.0
    }

    /**
     * 두 Geometry 간의 교차 여부 확인
     * 
     * @param geom1 첫 번째 Geometry
     * @param geom2 두 번째 Geometry
     * @return 교차하면 true
     */
    fun intersects(geom1: Geometry, geom2: Geometry): Boolean {
        return try {
            geom1.intersects(geom2)
        } catch (e: Exception) {
            logger.warn(e) { "Geometry 교차 검사 실패" }
            false
        }
    }

    // ===== 좌표계 및 캐시 =====

    /**
     * 좌표계 정보 추출 (WKT에서 SRID 파싱)
     * 
     * @param wkt WKT 문자열
     * @return SRID 번호 또는 null
     */
    fun extractSRID(wkt: String): Int? {
        val sridRegex = """SRID=(\d+);""".toRegex()
        return sridRegex.find(wkt)?.groupValues?.get(1)?.toIntOrNull()
    }
    
    /**
     * SRID 정보 확인
     * 
     * @param srid SRID 번호
     * @return 유효한 SRID이면 true
     */
    fun isValidSRID(srid: Int): Boolean {
        return srid in 1000..32767 // 일반적인 EPSG 코드 범위
    }

    /**
     * BBOX 기반 캐시 키 생성
     * 
     * @param minX 최소 X 좌표
     * @param minY 최소 Y 좌표
     * @param maxX 최대 X 좌표
     * @param maxY 최대 Y 좌표
     * @param prefix 캐시 키 접두사
     * @return 정규화된 캐시 키
     */
    fun generateCacheKey(minX: Double, minY: Double, maxX: Double, maxY: Double, prefix: String = "geo"): String {
        // 소수점 6자리로 정규화 (약 0.1m 정밀도)
        val normalizedCoords = listOf(minX, minY, maxX, maxY)
            .map { "%.6f".format(it) }
            .joinToString(",")
        
        return "$prefix:${normalizedCoords.hashCode().toString(16)}"
    }
}