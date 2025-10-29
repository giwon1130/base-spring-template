package com.template.platform

import com.template.platform.common.sse.SseManager
import com.template.platform.features.changeset.application.ChangesetService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 플랫폼 템플릿 통합 테스트
 * 
 * TestContainers를 사용한 실제 환경 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class PlatformTemplateIntegrationTest {
    
    @LocalServerPort
    private var port: Int = 0
    
    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    
    @Autowired
    private lateinit var sseManager: SseManager
    
    @Autowired
    private lateinit var changesetService: ChangesetService
    
    companion object {
        private val postgresImage = DockerImageName
            .parse("postgis/postgis:15-3.3")
            .asCompatibleSubstituteFor("postgres")

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(postgresImage)
            .withDatabaseName("platform_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-test-db.sql")
        
        @Container
        @JvmStatic
        val redis = GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)
        
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName)
            
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }
    
    @Test
    fun `애플리케이션이 정상적으로 시작된다`() {
        val response = restTemplate.getForEntity("http://localhost:$port/actuator/health", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
    }
    
    @Test
    fun `SSE 연결 상태를 조회할 수 있다`() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port/sse/status", 
            Map::class.java
        )
        
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body!!.containsKey("connectionCount"))
    }
    
    @Test
    fun `BBOX 영역의 변경사항을 조회할 수 있다`() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/changes?minx=126.9&miny=37.4&maxx=127.1&maxy=37.6",
            Map::class.java
        )
        
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body!!.containsKey("success"))
        assertEquals(true, response.body!!["success"])
    }
    
    @Test
    fun `변경사항 통계를 조회할 수 있다`() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/changes/statistics",
            Map::class.java
        )
        
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }
    
    @Test
    fun `잘못된 BBOX로 요청 시 에러가 발생한다`() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/changes?minx=200&miny=37.4&maxx=127.1&maxy=37.6",
            Map::class.java
        )
        
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}
