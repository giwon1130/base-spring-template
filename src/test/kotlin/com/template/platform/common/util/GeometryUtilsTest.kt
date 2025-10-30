package com.template.platform.common.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Polygon
import kotlin.math.abs

class GeometryUtilsTest {

    @Test
    fun `WKT 문자열을 Polygon으로 변환`() {
        // Given
        val wkt = "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))"

        // When
        val polygon = GeometryUtils.polygonFromWkt(wkt)

        // Then
        assertThat(polygon).isNotNull
        assertThat(polygon!!.srid).isEqualTo(4326)
        assertThat(polygon.coordinates).hasSize(5) // 닫힌 링
    }

    @Test
    fun `잘못된 WKT 형식은 예외 발생`() {
        // Given
        val invalidWkt = "INVALID WKT"

        // When & Then
        assertThatThrownBy { GeometryUtils.polygonFromWkt(invalidWkt) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("잘못된 WKT 형식입니다")
    }

    @Test
    fun `빈 WKT는 null 반환`() {
        // When & Then
        assertThat(GeometryUtils.polygonFromWkt(null)).isNull()
        assertThat(GeometryUtils.polygonFromWkt("")).isNull()
        assertThat(GeometryUtils.polygonFromWkt("   ")).isNull()
    }

    @Test
    fun `BBOX를 Polygon으로 변환`() {
        // Given
        val minX = 126.0
        val minY = 37.0
        val maxX = 127.0
        val maxY = 38.0

        // When
        val polygon = GeometryUtils.bboxToPolygon(minX, minY, maxX, maxY)

        // Then
        assertThat(polygon).isNotNull
        assertThat(polygon.srid).isEqualTo(4326)
        val envelope = polygon.envelopeInternal
        assertThat(envelope.minX).isEqualTo(minX)
        assertThat(envelope.minY).isEqualTo(minY)
        assertThat(envelope.maxX).isEqualTo(maxX)
        assertThat(envelope.maxY).isEqualTo(maxY)
    }

    @Test
    fun `좌표 배열을 Polygon으로 변환`() {
        // Given
        val coordinates = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 0.0),
            Coordinate(1.0, 1.0),
            Coordinate(0.0, 1.0),
            Coordinate(0.0, 0.0)
        )

        // When
        val polygon = GeometryUtils.coordinatesToPolygon(coordinates)

        // Then
        assertThat(polygon).isNotNull
        assertThat(polygon.srid).isEqualTo(4326)
        assertThat(polygon.coordinates).hasSize(5)
    }

    @Test
    fun `닫힌 링 보장 테스트`() {
        // Given
        val openCoords = arrayOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 0.0),
            Coordinate(1.0, 1.0),
            Coordinate(0.0, 1.0)
        )

        // When
        val closedCoords = GeometryUtils.ensureClosedRing(openCoords)

        // Then
        assertThat(closedCoords).hasSize(5)
        assertThat(closedCoords.first()).isEqualTo(closedCoords.last())
    }

    @Test
    fun `이미 닫힌 링은 그대로 반환`() {
        // Given
        val alreadyClosed = arrayOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 0.0),
            Coordinate(1.0, 1.0),
            Coordinate(0.0, 1.0),
            Coordinate(0.0, 0.0)
        )

        // When
        val result = GeometryUtils.ensureClosedRing(alreadyClosed)

        // Then
        assertThat(result).hasSize(5)
        assertThat(result).isEqualTo(alreadyClosed)
    }

    @Test
    fun `Point 생성 테스트`() {
        // Given
        val x = 126.9779692
        val y = 37.566535

        // When
        val point = GeometryUtils.createPoint(x, y)

        // Then
        assertThat(point).isNotNull
        assertThat(point.srid).isEqualTo(4326)
        assertThat(point.x).isEqualTo(x)
        assertThat(point.y).isEqualTo(y)
    }

    @Test
    fun `좌표 유효성 검증`() {
        // When & Then
        assertThat(GeometryUtils.isValidCoordinate(126.0, 37.0)).isTrue
        assertThat(GeometryUtils.isValidCoordinate(-180.0, -90.0)).isTrue
        assertThat(GeometryUtils.isValidCoordinate(180.0, 90.0)).isTrue
        
        assertThat(GeometryUtils.isValidCoordinate(181.0, 37.0)).isFalse
        assertThat(GeometryUtils.isValidCoordinate(126.0, 91.0)).isFalse
        assertThat(GeometryUtils.isValidCoordinate(-181.0, 37.0)).isFalse
        assertThat(GeometryUtils.isValidCoordinate(126.0, -91.0)).isFalse
    }

    @Test
    fun `BBOX 유효성 검증`() {
        // When & Then
        assertThat(GeometryUtils.isValidBBox(126.0, 37.0, 127.0, 38.0)).isTrue
        
        // 잘못된 순서
        assertThat(GeometryUtils.isValidBBox(127.0, 37.0, 126.0, 38.0)).isFalse
        assertThat(GeometryUtils.isValidBBox(126.0, 38.0, 127.0, 37.0)).isFalse
        
        // 범위 초과
        assertThat(GeometryUtils.isValidBBox(-181.0, 37.0, 127.0, 38.0)).isFalse
        assertThat(GeometryUtils.isValidBBox(126.0, -91.0, 127.0, 38.0)).isFalse
    }

    @Test
    fun `EPSG 코드 추출 테스트`() {
        // When & Then
        assertThat(GeometryUtils.extractEpsg("EPSG:4326")).isEqualTo(4326)
        assertThat(GeometryUtils.extractEpsg("EPSG::4326")).isEqualTo(4326)
        assertThat(GeometryUtils.extractEpsg("EPSG/4326")).isEqualTo(4326)
        assertThat(GeometryUtils.extractEpsg("4326")).isEqualTo(4326)
        
        assertThat(GeometryUtils.extractEpsgOrNull("EPSG:4326")).isEqualTo(4326)
        assertThat(GeometryUtils.extractEpsgOrNull("invalid")).isNull()
        assertThat(GeometryUtils.extractEpsgOrNull(null)).isNull()
    }

    @Test
    fun `Haversine 거리 계산 테스트`() {
        // Given - 서울시청과 부산시청의 대략적인 좌표
        val seoul_lon = 126.9779692
        val seoul_lat = 37.566535
        val busan_lon = 129.0756416
        val busan_lat = 35.1795543

        // When
        val distance = GeometryUtils.haversineDistance(seoul_lon, seoul_lat, busan_lon, busan_lat)

        // Then - 서울-부산 거리는 약 325km
        assertThat(distance).isBetween(320.0, 330.0)
    }

    @Test
    fun `캐시 키 생성 테스트`() {
        // Given
        val minX = 126.0
        val minY = 37.0
        val maxX = 127.0
        val maxY = 38.0

        // When
        val cacheKey1 = GeometryUtils.generateCacheKey(minX, minY, maxX, maxY)
        val cacheKey2 = GeometryUtils.generateCacheKey(minX, minY, maxX, maxY, "custom")

        // Then
        assertThat(cacheKey1).startsWith("geo:")
        assertThat(cacheKey2).startsWith("custom:")
        
        // 같은 좌표는 같은 키 생성
        val duplicateKey = GeometryUtils.generateCacheKey(minX, minY, maxX, maxY)
        assertThat(cacheKey1).isEqualTo(duplicateKey)
    }

    @Test
    fun `Polygon 확장 함수 테스트`() {
        // Given
        val polygon = GeometryUtils.bboxToPolygon(126.0, 37.0, 127.0, 38.0)

        // When
        val wkt = polygon.toWKT()
        val metrics = polygon.extractRectangleMetrics()

        // Then
        assertThat(wkt).isNotBlank
        assertThat(wkt).startsWith("POLYGON")
        
        assertThat(metrics.widthKm).isGreaterThan(0.0)
        assertThat(metrics.heightKm).isGreaterThan(0.0)
        assertThat(metrics.areaKm2).isGreaterThan(0.0)
        assertThat(metrics.dimensionText).contains("km x")
    }

    @Test
    fun `좌표 리스트 확장 함수 테스트`() {
        // Given
        val coordinates = listOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 0.0),
            Coordinate(1.0, 1.0),
            Coordinate(0.0, 1.0),
            Coordinate(0.0, 0.0)
        )

        // When
        val polygon = coordinates.toPolygon()

        // Then
        assertThat(polygon).isInstanceOf(Polygon::class.java)
        assertThat(polygon.coordinates).hasSize(5)
    }
}