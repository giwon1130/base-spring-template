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
import java.util.UUID

@SpringBootTest
@ActiveProfiles(profiles = ["local"], inheritProfiles = false)
@org.springframework.test.context.TestPropertySource(
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
        "jwt.expiration=3600000"
    ]
)
class AuthServiceIntegrationTest @Autowired constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val dataSource: javax.sql.DataSource
) {

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
        assertThat(found.get().password).isNotEqualTo(request.password) // 암호화 확인
    }
}
