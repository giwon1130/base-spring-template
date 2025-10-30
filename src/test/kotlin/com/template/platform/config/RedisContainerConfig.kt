package com.template.platform.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class RedisContainerConfig {

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): GenericContainer<*> {
        val redisContainer = GenericContainer(DockerImageName.parse("redis:6.2"))
            .apply {
                withExposedPorts(6379)
            }

        // 컨테이너 시작 후 포트 매핑을 가져옴
        redisContainer.start()

        // Redis 컨테이너의 IP와 포트를 가져와서 프로퍼티 설정
        setRedisProperties(redisContainer)

        return redisContainer
    }

    private fun setRedisProperties(redisContainer: GenericContainer<*>) {
        val containerIp = redisContainer.host
        val containerPort = redisContainer.getMappedPort(6379)

        System.setProperty("spring.data.redis.host", containerIp)
        System.setProperty("spring.data.redis.port", containerPort.toString())
    }
}