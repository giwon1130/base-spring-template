package com.template.platform.bootstrap.config

import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka

/**
 * Kafka 설정
 * 
 * 기본 설정은 application.yml에서 관리
 * 필요시 추가 커스터마이징 가능
 */
@Configuration
@EnableKafka
class KafkaConfig