package com.template.platform.support

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import javax.sql.DataSource

@SpringBootTest
@ActiveProfiles(profiles = ["local"], inheritProfiles = false)
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:postgresql://localhost:35432/template_db",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6380",
        "spring.data.redis.key.refresh-token=platform:auth:refresh:",
        "spring.flyway.clean-disabled=false",
        "jwt.secret=integration-test-secret",
        "jwt.expiration=3600000",
        "scene.presigned.default-secret=test-scene-secret",
        "scene.presigned.expires-seconds=600",
        "scene.download.scene-path=/api/v1/scenes/change-detections",
        "scene.download.label-path=/api/v1/scenes/change-detections",
        "scene.download.scene-secret=test-scene-secret",
        "scene.download.label-secret=test-scene-secret"
    ]
)
abstract class IntegrationTestSupport {

    @Autowired
    private lateinit var dataSource: DataSource

    @BeforeEach
    fun resetDatabase() {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .load()
        flyway.clean()
        flyway.migrate()
    }
}
