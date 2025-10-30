package com.template.platform.common.util

import mu.KotlinLogging
import org.locationtech.jts.geom.*
import org.locationtech.jts.io.ParseException
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

/**
 * JTS Geometry 통합 유틸리티
 * 
 * 주요 기능:
 * - WKT ↔ Geometry 변환 (유틸리티 객체 + 확장 함수)
 * - BBOX 처리 (생성, 변환, 검증)
 * - Polygon 생성 및 검증
 * - 좌표계 검증 (SRID, EPSG 코드 처리)
 * - 안전한 링 처리 (닫힌 링 보장)
 * - 캐시 키 생성 (좌표 기반)
 * - Geometry 유효성 검증 및 면적 계산
 * - 교집합 계산 및 거리 측정 (Haversine)
 * - 고급 좌표 처리 확장 함수
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

    // ===== EPSG 코드 처리 =====
    
    /**
     * EPSG 코드 추출 (다양한 형식 지원)
     * 
     * @param crs 좌표계 문자열
     * @return EPSG 코드 (숫자)
     */
    fun extractEpsg(crs: String): Int {
        val s = crs.trim()

        // AUTHORITY["EPSG","xxxx"]
        Regex("""AUTHORITY\["EPSG"\s*,\s*"(\d+)"\]""", RegexOption.IGNORE_CASE)
            .findAll(s).lastOrNull()
            ?.let { return it.groupValues[1].toInt() }

        // EPSG:xxxx / EPSG::xxxx / EPSG/xxxx
        Regex("""EPSG[:/]{1,2}(\d+)""", RegexOption.IGNORE_CASE)
            .find(s)
            ?.let { return it.groupValues[1].toInt() }

        // 그냥 숫자
        Regex("""\b(\d{3,6})\b""")
            .find(s)
            ?.let { return it.groupValues[1].toInt() }

        throw IllegalArgumentException("EPSG code not found: $crs")
    }

    /**
     * EPSG 코드 추출 (안전 버전)
     * 
     * @param crs 좌표계 문자열
     * @return EPSG 코드 또는 null
     */
    fun extractEpsgOrNull(crs: String?): Int? =
        crs?.let { runCatching { extractEpsg(it) }.getOrNull() }

    // ===== 거리 계산 =====
    
    /**
     * 두 위경도 좌표 간 거리(km)를 구하는 Haversine 공식
     * 
     * @param lon1 첫 번째 경도
     * @param lat1 첫 번째 위도
     * @param lon2 두 번째 경도
     * @param lat2 두 번째 위도
     * @return 거리 (킬로미터)
     */
    fun haversineDistance(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double {
        val R = 6371.0088 // 지구 평균 반지름 (km)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = kotlin.math.sin(dLat / 2).pow(2.0) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2).pow(2.0)

        val c = 2 * kotlin.math.asin(kotlin.math.sqrt(a))
        return R * c
    }
}

// ===== Kotlin 확장 함수들 =====

/**
 * Geometry를 WKT 문자열로 변환하는 확장 함수
 */
fun Geometry.toWKT(): String = WKTWriter().write(this)

/**
 * WKT 문자열을 Geometry로 변환하는 확장 함수
 */
fun String.toGeometry(): Geometry = WKTReader().read(this)

/**
 * WKT projection string에서 EPSG 코드를 추출하는 확장 함수
 * 
 * @return EPSG 코드 (예: "EPSG:4326") 또는 null
 */
fun String?.extractEpsgCode(): String? {
    if (this.isNullOrBlank()) return null
    
    return runCatching {
        val epsgRegex = """AUTHORITY\s*\[\s*"EPSG"\s*,\s*"(\d+)"\s*\]""".toRegex()
        epsgRegex.findAll(this).lastOrNull()?.groupValues?.get(1)?.let { "EPSG:$it" }
    }.getOrElse { 
        KotlinLogging.logger {}.warn { "EPSG 코드 추출 실패: $it" }
        null 
    }
}

/**
 * Geometry의 모든 좌표 목록을 가져오는 확장 함수
 */
fun Geometry.getAllCoordinates(): List<Coordinate> = when (this) {
    is Polygon -> buildList {
        addAll(exteriorRing.coordinates)
        repeat(numInteriorRing) { i ->
            addAll(getInteriorRingN(i).coordinates)
        }
    }
    is MultiPolygon -> buildList {
        repeat(numGeometries) { i ->
            val polygon = getGeometryN(i) as Polygon
            addAll(polygon.exteriorRing.coordinates)
            repeat(polygon.numInteriorRing) { j ->
                addAll(polygon.getInteriorRingN(j).coordinates)
            }
        }
    }
    else -> {
        KotlinLogging.logger {}.warn { "지원하지 않는 Geometry 타입: $geometryType" }
        emptyList()
    }
}

/**
 * Geometry의 중심점을 가져오는 확장 함수
 */
fun Geometry.getCenterPoint(): Point? = when (this) {
    is Polygon, is MultiPolygon -> centroid
    else -> null
}

/**
 * 좌표 리스트를 Polygon으로 변환하는 확장 함수
 */
fun List<Coordinate>.toPolygon(): Polygon {
    val coordArray = this.toTypedArray()
    val exteriorRing = GeometryFactory().createLinearRing(coordArray)
    return GeometryFactory().createPolygon(exteriorRing, null)
}

/**
 * 두 Geometry의 교집합 영역을 계산하는 확장 함수
 */
fun Geometry.calculateIntersectionWith(other: Geometry): Geometry {
    val intersection = this.intersection(other)
    return when (intersection) {
        is LineString -> intersection.buffer(0.0) // LineString을 Polygon으로 변환
        else -> intersection
    }
}

/**
 * 교집합 비율을 계산하는 확장 함수
 * 
 * @param intersectionArea 교집합 영역
 * @return 교집합 비율 (퍼센트)
 */
fun Geometry.calculateIntersectionPercentage(intersectionArea: Geometry): BigDecimal {
    val intersectionSize = BigDecimal.valueOf(intersectionArea.area)
    val thisArea = BigDecimal.valueOf(this.area)

    return thisArea.takeIf { it > BigDecimal.ZERO }?.let { area ->
        intersectionSize
            .divide(area, 6, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP)
    } ?: BigDecimal.ZERO.setScale(2)
}

// ===== 데이터 클래스 =====

/**
 * 사각형 메트릭 정보
 */
data class QuadMetricsDto(
    val widthKm: Double,
    val heightKm: Double,
    val areaKm2: Double,
    val dimensionText: String
)

/**
 * Polygon을 직사각형으로 가정하고 메트릭을 계산하는 확장 함수
 */
fun Polygon.extractRectangleMetrics(): QuadMetricsDto {
    val env = envelopeInternal
    val midLat = (env.minY + env.maxY) / 2.0

    val width = GeometryUtils.haversineDistance(env.minX, midLat, env.maxX, midLat)
    val height = GeometryUtils.haversineDistance(env.minX, env.minY, env.minX, env.maxY)
    val area = width * height
    val dimensionText = "%.1fkm x %.1fkm".format(width, height)

    return QuadMetricsDto(
        widthKm = width,
        heightKm = height,
        areaKm2 = area,
        dimensionText = dimensionText
    )
}