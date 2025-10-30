package com.template.platform.common.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.*

class DateTimeUtilsTest {

    @Test
    fun `ISO-8601 문자열을 LocalDateTime으로 파싱`() {
        // Given
        val isoString = "2024-03-15T12:34:56Z"

        // When
        val result = DateTimeUtils.parseIsoDateTime(isoString)

        // Then
        assertThat(result.year).isEqualTo(2024)
        assertThat(result.month).isEqualTo(Month.MARCH)
        assertThat(result.dayOfMonth).isEqualTo(15)
        assertThat(result.hour).isEqualTo(12)
        assertThat(result.minute).isEqualTo(34)
        assertThat(result.second).isEqualTo(56)
    }

    @Test
    fun `ISO-8601 오프셋 포함 문자열 파싱`() {
        // Given
        val isoString = "2024-03-15T12:34:56+09:00"

        // When
        val result = DateTimeUtils.parseIsoDateTime(isoString)

        // Then
        assertThat(result.year).isEqualTo(2024)
        assertThat(result.month).isEqualTo(Month.MARCH)
        assertThat(result.dayOfMonth).isEqualTo(15)
        // 오프셋이 제거되고 LocalDateTime으로 변환됨
        assertThat(result.hour).isEqualTo(12)
    }

    @Test
    fun `잘못된 ISO 형식은 예외 발생`() {
        // Given
        val invalidIsoString = "2024-13-40T25:70:90Z"

        // When & Then
        assertThatThrownBy { DateTimeUtils.parseIsoDateTime(invalidIsoString) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("날짜 형식이 올바르지 않습니다")
    }

    @Test
    fun `ISO-8601 문자열을 Instant로 파싱`() {
        // Given
        val isoString = "2024-03-15T12:34:56Z"

        // When
        val result = DateTimeUtils.parseIsoToInstant(isoString)

        // Then
        assertThat(result).isNotNull
        assertThat(result.epochSecond).isGreaterThan(0)
    }

    @Test
    fun `현재 시간을 ISO 문자열로 변환`() {
        // When
        val result = DateTimeUtils.nowAsIsoString()

        // Then
        assertThat(result).isNotBlank
        assertThat(result).endsWith("Z")
        assertThat(result).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*Z")
    }

    @Test
    fun `LocalDateTime을 ISO 문자열로 변환`() {
        // Given
        val dateTime = LocalDateTime.of(2024, 3, 15, 12, 34, 56)

        // When
        val result = DateTimeUtils.toIsoString(dateTime)

        // Then
        assertThat(result).isEqualTo("2024-03-15T12:34:56Z")
    }

    @Test
    fun `Instant를 ISO 문자열로 변환`() {
        // Given
        val instant = Instant.parse("2024-03-15T12:34:56Z")

        // When
        val result = DateTimeUtils.toIsoString(instant)

        // Then
        assertThat(result).isEqualTo("2024-03-15T12:34:56Z")
    }

    @Test
    fun `사용자 친화적 형식으로 포맷`() {
        // Given
        val dateTime = LocalDateTime.of(2024, 3, 15, 12, 34, 56)

        // When
        val result = DateTimeUtils.formatUserFriendly(dateTime)

        // Then
        assertThat(result).isEqualTo("2024-03-15 12:34")
    }

    @Test
    fun `두 날짜 사이의 일수 계산`() {
        // Given
        val start = LocalDate.of(2024, 3, 15)
        val end = LocalDate.of(2024, 3, 20)

        // When
        val result = DateTimeUtils.daysBetween(start, end)

        // Then
        assertThat(result).isEqualTo(5)
    }

    @Test
    fun `같은 날짜의 일수는 0`() {
        // Given
        val date = LocalDate.of(2024, 3, 15)

        // When
        val result = DateTimeUtils.daysBetween(date, date)

        // Then
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `현재 시간에서 지정된 분만큼 이후 시간 계산`() {
        // Given
        val minutes = 30L
        val beforeCall = Instant.now()

        // When
        val result = DateTimeUtils.minutesFromNow(minutes)
        val afterCall = Instant.now()

        // Then
        val expectedMin = beforeCall.plusSeconds(minutes * 60)
        val expectedMax = afterCall.plusSeconds(minutes * 60)
        
        assertThat(result).isBetween(expectedMin, expectedMax)
    }

    @Test
    fun `UTC 시간을 한국 시간으로 변환`() {
        // Given
        val utcInstant = Instant.parse("2024-03-15T12:00:00Z")

        // When
        val koreaTime = DateTimeUtils.toKoreaTime(utcInstant)

        // Then
        // UTC 12:00 = KST 21:00 (UTC+9)
        assertThat(koreaTime.hour).isEqualTo(21)
        assertThat(koreaTime.year).isEqualTo(2024)
        assertThat(koreaTime.month).isEqualTo(Month.MARCH)
        assertThat(koreaTime.dayOfMonth).isEqualTo(15)
    }

    @Test
    fun `한국 시간을 UTC로 변환`() {
        // Given
        val koreaDateTime = LocalDateTime.of(2024, 3, 15, 21, 0, 0)

        // When
        val utcInstant = DateTimeUtils.toUtc(koreaDateTime)

        // Then
        // KST 21:00 = UTC 12:00 (UTC+9)
        val utcDateTime = utcInstant.atZone(ZoneOffset.UTC).toLocalDateTime()
        assertThat(utcDateTime.hour).isEqualTo(12)
        assertThat(utcDateTime.year).isEqualTo(2024)
        assertThat(utcDateTime.month).isEqualTo(Month.MARCH)
        assertThat(utcDateTime.dayOfMonth).isEqualTo(15)
    }

    @Test
    fun `한국 시간과 UTC 상호 변환 일관성 테스트`() {
        // Given
        val originalKoreaTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45)

        // When
        val utcInstant = DateTimeUtils.toUtc(originalKoreaTime)
        val convertedBackToKorea = DateTimeUtils.toKoreaTime(utcInstant)

        // Then
        assertThat(convertedBackToKorea).isEqualTo(originalKoreaTime)
    }

    @Test
    fun `ISO 파싱과 변환의 일관성 테스트`() {
        // Given
        val isoString = "2024-03-15T12:34:56Z"

        // When
        val instant = DateTimeUtils.parseIsoToInstant(isoString)
        val convertedBack = DateTimeUtils.toIsoString(instant)

        // Then
        assertThat(convertedBack).isEqualTo(isoString)
    }
}