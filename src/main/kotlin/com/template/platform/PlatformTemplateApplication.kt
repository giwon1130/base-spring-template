package com.template.platform

import com.template.platform.bootstrap.security.CustomUserDetails
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

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
@EnableJpaAuditing
class PlatformTemplateApplication {

    @Bean
    fun auditorProvider(): AuditorAware<Long> = AuditorAware {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication != null && authentication.isAuthenticated) {
            val principal = authentication.principal
            val userId = when (principal) {
                is CustomUserDetails -> principal.getUserId()
                is String -> if (principal == "anonymousUser") -1L else -1L
                else -> -1L
            }
            if (userId >= 0) Optional.of(userId) else Optional.empty()
        } else {
            Optional.empty()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<PlatformTemplateApplication>(*args)
}
