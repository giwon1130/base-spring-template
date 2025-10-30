package com.template.platform.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class DatabaseContainerConfig {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> {
        return PostgreSQLContainer(
            DockerImageName
                .parse("postgis/postgis:15-3.5")
                .asCompatibleSubstituteFor("postgres")
        ).apply {
            withDatabaseName("template_test_db")
            withUsername("test_user")
            withPassword("test_password")
        }
    }
}