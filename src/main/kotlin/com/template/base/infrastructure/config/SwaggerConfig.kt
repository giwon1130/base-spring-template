package com.template.base.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Spring Boot Base Template API")
                    .description("BMOA 기반 Spring Boot 템플릿 API 문서")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Template Team")
                            .url("https://github.com/giwon1130/spring-boot-base-template")
                    )
            )
    }
}