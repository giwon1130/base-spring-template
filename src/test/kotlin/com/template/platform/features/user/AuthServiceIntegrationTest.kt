package com.template.platform.features.user

import com.template.platform.features.user.application.AuthService
import com.template.platform.features.user.domain.Role
import com.template.platform.features.user.domain.UserRepository
import com.template.platform.features.user.presentation.request.RegisterRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.UUID
import javax.sql.DataSource

@SpringBootTest
@Testcontainers
@ActiveProfiles(profiles = ["test"], inheritProfiles = false)
@org.springframework.test.context.TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.data.redis.key.refresh-token=platform:auth:refresh:",
        "spring.flyway.clean-disabled=false",
        "jwt.secret=integration-test-secret",
        "jwt.expiration=3600000"
    ]
)
class AuthServiceIntegrationTest @Autowired constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val dataSource: DataSource
) {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(
            DockerImageName.parse("postgis/postgis:15-3.5").asCompatibleSubstituteFor("postgres")
        ).apply {
            withDatabaseName("template_db")
            withUsername("postgres")
            withPassword("postgres")
        }

        @Container
        @JvmStatic
        val redis = GenericContainer(DockerImageName.parse("redis:7-alpine")).apply {
            withExposedPorts(6379)
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }

    @BeforeEach
    fun setup() {
        val flyway = org.flywaydb.core.Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .load()
        flyway.clean()
        flyway.migrate()
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `회원가입을 수행하면 사용자 정보가 저장된다`() {
        val request = RegisterRequest(
            email = "tester-${UUID.randomUUID()}@example.com",
            password = "password123!",
            name = "통합테스트",
            role = Role.USER
        )

        val savedUser = authService.registerUser(request)

        val found = userRepository.findById(savedUser.userId)
        assertThat(found).isPresent
        assertThat(found.get().email).isEqualTo(request.email)
        assertThat(found.get().name).isEqualTo(request.name)
        assertThat(found.get().password).isNotEqualTo(request.password)
    }
}
