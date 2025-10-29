package com.template.platform.bootstrap.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

/**
 * Swagger/OpenAPI 3.0 설정
 * 
 * 주요 기능:
 * - API 문서 자동 생성
 * - JWT Bearer Token 인증 지원
 * - 프로젝트 정보 및 연락처 설정
 * 
 * 접속 URL: http://localhost:8081/swagger-ui/index.html
 */
@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Platform Template API",
        version = "1.0.0",
        description = "Spring Boot Base Template API - JWT 인증, 사용자/Scene/AOI 관리, SSE 알림, Redis 캐시 기능 제공",
        contact = Contact(
            name = "Platform Development Team",
            email = "dev@platform.com"
        )
    ),
    security = [
        SecurityRequirement(name = "bearerAuth")
    ]
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다."
)
class SwaggerConfig