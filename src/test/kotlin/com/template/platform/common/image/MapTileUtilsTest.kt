package com.template.platform.common.image

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.abs

class MapTileUtilsTest {

    @Test
    fun `서울시청 좌표 변환 테스트`() {
        // Given: 서울시청 좌표
        val seoulLon = 126.9784
        val seoulLat = 37.5665
        val zoom = 15

        // When: 타일 좌표로 변환
        val (tileX, tileY) = MapTileUtils.lonLatToTileXY(seoulLon, seoulLat, zoom)

        // Then: 예상되는 타일 좌표 범위 확인
        assertTrue(tileX >= 0)
        assertTrue(tileY >= 0)
        assertTrue(tileX < (1 shl zoom)) // 2^zoom 미만
        assertTrue(tileY < (1 shl zoom))
    }

    @Test
    fun `픽셀 좌표 변환 테스트`() {
        // Given: 서울시청 좌표
        val seoulLon = 126.9784
        val seoulLat = 37.5665
        val zoom = 15

        // When: 픽셀 좌표로 변환
        val pixelX = MapTileUtils.lonToPixelX(seoulLon, zoom)
        val pixelY = MapTileUtils.latToPixelY(seoulLat, zoom)

        // Then: 픽셀 좌표가 양수여야 함
        assertTrue(pixelX >= 0)
        assertTrue(pixelY >= 0)
    }

    @Test
    fun `역변환 테스트`() {
        // Given: 원본 좌표
        val originalLon = 126.9784
        val originalLat = 37.5665
        val zoom = 15

        // When: 픽셀로 변환 후 다시 좌표로 변환
        val pixelX = MapTileUtils.lonToPixelX(originalLon, zoom)
        val pixelY = MapTileUtils.latToPixelY(originalLat, zoom)
        val convertedLon = MapTileUtils.pixelXToLon(pixelX, zoom)
        val convertedLat = MapTileUtils.pixelYToLat(pixelY, zoom)

        // Then: 원본과 거의 같아야 함 (부동소수점 오차 고려)
        assertEquals(originalLon, convertedLon, 0.001)
        assertEquals(originalLat, convertedLat, 0.001)
    }

    @Test
    fun `거리 계산 테스트`() {
        // Given: 서울시청과 부산시청 좌표
        val seoulLon = 126.9784
        val seoulLat = 37.5665
        val busanLon = 129.0756
        val busanLat = 35.1796

        // When: 거리 계산
        val distance = MapTileUtils.calculateDistance(seoulLon, seoulLat, busanLon, busanLat)

        // Then: 약 325km 정도여야 함 (오차 ±10km)
        assertTrue(distance > 315000) // 315km 이상
        assertTrue(distance < 335000) // 335km 미만
    }

    @Test
    fun `같은 지점 거리 계산 테스트`() {
        // Given: 같은 좌표
        val lon = 126.9784
        val lat = 37.5665

        // When: 같은 지점 간 거리 계산
        val distance = MapTileUtils.calculateDistance(lon, lat, lon, lat)

        // Then: 거리가 0에 가까워야 함
        assertTrue(distance < 1.0) // 1미터 미만
    }

    @Test
    fun `미터당 픽셀 계산 테스트`() {
        // Given: 서울 지역 좌표와 줌 레벨
        val lat = 37.5665
        val zoom = 15

        // When: 미터당 픽셀 계산
        val metersPerPixel = MapTileUtils.metersPerPixel(lat, zoom)

        // Then: 양수여야 하고 합리적인 범위여야 함
        assertTrue(metersPerPixel > 0)
        assertTrue(metersPerPixel < 1000) // 줌 15에서는 1000m/pixel 미만이어야 함
    }

    @Test
    fun `타일 좌표 경계값 테스트`() {
        // Given: 극한 좌표들 (경도 180은 제외, 179.9999 사용)
        val testCases = listOf(
            Triple(-180.0, -85.0, 10),   // 최서남단
            Triple(179.9999, 85.0, 10),  // 최동북단 (180도는 경계값이라 범위 초과)
            Triple(0.0, 0.0, 10)         // 적도와 본초자오선 교차점
        )

        testCases.forEach { (lon, lat, zoom) ->
            // When: 타일 좌표 변환
            val (tileX, tileY) = MapTileUtils.lonLatToTileXY(lon, lat, zoom)

            // Then: 유효한 범위 내에 있어야 함
            assertTrue(tileX >= 0, "tileX should be >= 0 for lon=$lon")
            assertTrue(tileY >= 0, "tileY should be >= 0 for lat=$lat")
            assertTrue(tileX < (1 shl zoom), "tileX should be < ${1 shl zoom} for zoom=$zoom, actual tileX=$tileX")
            assertTrue(tileY < (1 shl zoom), "tileY should be < ${1 shl zoom} for zoom=$zoom, actual tileY=$tileY")
        }
    }
}