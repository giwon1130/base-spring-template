package com.template.platform.bootstrap.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * 데이터베이스 설정
 */
@Configuration
@EnableJpaRepositories(basePackages = ["com.template.platform"])
@EntityScan(basePackages = ["com.template.platform"])
@EnableTransactionManagement
class DatabaseConfig