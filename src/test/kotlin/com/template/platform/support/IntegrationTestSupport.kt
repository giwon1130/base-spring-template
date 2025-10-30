package com.template.platform.support

import com.template.platform.config.DatabaseContainerConfig
import com.template.platform.config.RedisContainerConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ActiveProfiles(profiles = ["test"], inheritProfiles = false)
@ContextConfiguration(classes = [DatabaseContainerConfig::class, RedisContainerConfig::class])
abstract class IntegrationTestSupport {
    // TestContainers를 사용한 실제 PostgreSQL + Redis 환경에서 테스트
    // 각 테스트는 독립적인 컨테이너 환경에서 실행됨
}
