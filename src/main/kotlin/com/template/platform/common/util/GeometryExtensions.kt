package com.template.platform.common.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.locationtech.jts.geom.*
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter
// GeoJSON 기능은 추가 의존성이 필요하므로 주석 처리
// import org.locationtech.jts.io.geojson.GeoJsonReader 
// import org.locationtech.jts.io.geojson.GeoJsonWriter
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

/**
 * JTS Geometry Kotlin 확장 함수들
 * 
 * 주요 기능:
 * - Geometry ↔ WKT/GeoJSON 변환 확장 함수
 * - EPSG 코드 추출
 * - 좌표 처리 및 변환
 * - 교집합 계산
 * - 거리 계산 (Haversine)
 * - JSON 직렬화/역직렬화
 */

private val logger = KotlinLogging.logger {}
private val geometryFactory = GeometryFactory()

// ===== WKT 변환 확장 함수 =====

/**
 * Geometry를 WKT 문자열로 변환
 */
fun Geometry.toWKT(): String = WKTWriter().write(this)

/**
 * WKT 문자열을 Geometry로 변환
 */
fun String.toGeometry(): Geometry = WKTReader().read(this)

// ===== GeoJSON 변환 확장 함수 (추가 의존성 필요시 활성화) =====

/**
 * Geometry를 GeoJSON Map으로 변환
 * 
 * 참고: GeoJSON 기능을 사용하려면 추가 의존성이 필요합니다:
 * implementation 'org.locationtech.jts:jts-io-common:1.19.0'
 */
/*
fun Geometry.toGeoJsonMap(): Map<String, Any> {
    val writer = GeoJsonWriter()
    val json = writer.write(this)
    return jacksonObjectMapper().readValue(json, Map::class.java) as Map<String, Any>
}
*/

// ===== EPSG 코드 추출 =====

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
        logger.warn { "EPSG 코드 추출 실패: $it" }
        null 
    }
}

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

// ===== 좌표 처리 확장 함수 =====

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
        logger.warn { "지원하지 않는 Geometry 타입: $geometryType" }
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
    val exteriorRing = geometryFactory.createLinearRing(coordArray)
    return geometryFactory.createPolygon(exteriorRing, null)
}

// ===== 교집합 계산 =====

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

// ===== JSON 직렬화 =====

/**
 * Jackson용 Geometry 역직렬화기 (GeoJSON 의존성 필요시 활성화)
 */
/*
class GeometryDeserializer : JsonDeserializer<Geometry>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Geometry {
        val node: JsonNode = p.codec.readTree(p)
        val geoJson = node.toString()  // 전체 geometry 필드 JSON
        val reader = GeoJsonReader()
        return reader.read(geoJson)
    }
}
*/

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

    val width = haversineDistance(env.minX, midLat, env.maxX, midLat)
    val height = haversineDistance(env.minX, env.minY, env.minX, env.maxY)
    val area = width * height
    val dimensionText = "%.1fkm x %.1fkm".format(width, height)

    return QuadMetricsDto(
        widthKm = width,
        heightKm = height,
        areaKm2 = area,
        dimensionText = dimensionText
    )
}