package com.template.platform

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

/**
 * 공통 플랫폼 템플릿 애플리케이션
 * 
 * 주요 기능:
 * - SSE 실시간 알림 (Redis Pub/Sub 지원)
 * - 캐시 + 무효화 (Redis)
 * - Kafka 멱등성 보조
 * - Outbox 패턴 골격
 * - GIS 유틸리티 (PostGIS + JTS)
 * - 표준 에러 체계
 */
@SpringBootApplication
@EnableCaching
class PlatformTemplateApplication

fun main(args: Array<String>) {
    runApplication<PlatformTemplateApplication>(*args)
}