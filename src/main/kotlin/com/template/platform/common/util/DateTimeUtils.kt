package com.template.platform.common.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 날짜/시간 처리 유틸리티
 * 
 * 주요 기능:
 * - ISO-8601 문자열 파싱
 * - 다양한 포맷 지원
 * - 시간대 변환
 * - 날짜 계산
 */
object DateTimeUtils {
    
    /**
     * ISO-8601 문자열을 LocalDateTime으로 파싱
     * 
     * @param input ISO-8601 형식 문자열 (예: "2024-03-15T12:34:56Z")
     * @return LocalDateTime 객체
     * @throws DateTimeParseException 파싱 실패 시
     */
    fun parseIsoDateTime(input: String): LocalDateTime {
        return try {
            OffsetDateTime.parse(input).toLocalDateTime()
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("날짜 형식이 올바르지 않습니다: $input", e)
        }
    }
    
    /**
     * ISO-8601 문자열을 Instant로 파싱
     * 
     * @param input ISO-8601 형식 문자열
     * @return Instant 객체
     */
    fun parseIsoToInstant(input: String): Instant {
        return try {
            Instant.parse(input)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("날짜 형식이 올바르지 않습니다: $input", e)
        }
    }
    
    /**
     * 현재 시간을 ISO-8601 문자열로 변환
     * 
     * @return ISO-8601 형식 문자열 (UTC 기준)
     */
    fun nowAsIsoString(): String {
        return Instant.now().toString()
    }
    
    /**
     * LocalDateTime을 ISO-8601 문자열로 변환
     * 
     * @param dateTime LocalDateTime 객체
     * @return ISO-8601 형식 문자열
     */
    fun toIsoString(dateTime: LocalDateTime): String {
        return dateTime.atZone(ZoneOffset.UTC).toInstant().toString()
    }
    
    /**
     * Instant를 ISO-8601 문자열로 변환
     * 
     * @param instant Instant 객체
     * @return ISO-8601 형식 문자열
     */
    fun toIsoString(instant: Instant): String {
        return instant.toString()
    }
    
    /**
     * 날짜를 사용자 친화적인 형식으로 포맷
     * 
     * @param dateTime LocalDateTime 객체
     * @return 포맷된 문자열 (예: "2024-03-15 12:34")
     */
    fun formatUserFriendly(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }
    
    /**
     * 두 날짜 사이의 일수 계산
     * 
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @return 일수 차이
     */
    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays()
    }
    
    /**
     * 현재 시간에서 지정된 분만큼 이후 시간 계산
     * 
     * @param minutes 분 단위
     * @return Instant 객체
     */
    fun minutesFromNow(minutes: Long): Instant {
        return Instant.now().plusSeconds(minutes * 60)
    }
    
    /**
     * UTC 시간을 한국 시간으로 변환
     * 
     * @param utcInstant UTC 시간
     * @return 한국 시간 (KST)
     */
    fun toKoreaTime(utcInstant: Instant): LocalDateTime {
        return utcInstant.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
    }
    
    /**
     * 한국 시간을 UTC로 변환
     * 
     * @param koreaDateTime 한국 시간
     * @return UTC Instant
     */
    fun toUtc(koreaDateTime: LocalDateTime): Instant {
        return koreaDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant()
    }
}