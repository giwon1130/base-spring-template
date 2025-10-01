package com.template.base

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * Spring Boot 기본 템플릿 애플리케이션
 * 
 * 이 템플릿은 다음 기능들을 포함합니다:
 * - JWT 기반 인증/인가
 * - 사용자 관리 (회원가입, 로그인, 내정보 조회/수정)
 * - Redis 캐싱
 * - PostgreSQL 데이터베이스
 * - Swagger API 문서화
 * - 로깅 및 트레이싱
 */
@SpringBootApplication
@EnableJpaAuditing
class BaseTemplateApplication

fun main(args: Array<String>) {
    runApplication<BaseTemplateApplication>(*args)
}