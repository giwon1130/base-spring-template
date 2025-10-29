# Migration Guide

기존 프로젝트에서 Platform Template으로 마이그레이션하는 가이드입니다.

## 📋 3단계 마이그레이션 프로세스

### 1단계: 의존성 추가

#### Gradle 설정 (Composite Build 방식)

**settings.gradle.kts**
```kotlin
includeBuild("../spring-boot-base-template")
```

**build.gradle.kts**
```kotlin
dependencies {
    implementation("com.template:spring-boot-base-template")
    
    // 기존 dependencies는 유지
}
```

#### 직접 의존성 방식 (JAR 배포)
```kotlin
dependencies {
    implementation("com.template:platform-template:1.0.0")
}
```

### 2단계: 설정 키 맞추기

#### Database 설정
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    
  jpa:
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect
```

#### Redis 설정
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      timeout: 3s
      
  cache:
    type: redis
    redis:
      time-to-live: 3600s
```

#### Kafka 설정
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    consumer:
      group-id: ${spring.application.name}
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

### 3단계: 대표 기능 교체

#### A. SSE 알림 시스템 교체

**기존 코드**
```kotlin
// 기존 방식
@RestController
class OldNotificationController {
    private val emitters = mutableMapOf<String, SseEmitter>()
    
    @GetMapping("/notifications")
    fun subscribe(@RequestParam userId: String): SseEmitter {
        val emitter = SseEmitter()
        emitters[userId] = emitter
        return emitter
    }
}
```

**마이그레이션 후**
```kotlin
// Platform Template 사용
@RestController
class NotificationController(
    private val notificationService: NotificationService
) {
    @GetMapping("/sse/subscribe")
    fun subscribe(@RequestParam userId: String): SseEmitter {
        return sseManager.createConnection(userId)
    }
    
    fun sendAlert(userId: String, message: String) {
        notificationService.sendToUser(userId, "alert", mapOf("message" to message))
    }
}
```

#### B. 캐시 시스템 교체

**기존 코드**
```kotlin
@Service
class OldDataService {
    @Cacheable("data")
    fun getData(key: String): Data {
        return repository.findByKey(key)
    }
    
    fun updateData(key: String, data: Data) {
        repository.save(data)
        // 수동 캐시 무효화
        cacheManager.getCache("data")?.evict(key)
    }
}
```

**마이그레이션 후**
```kotlin
@Service
class DataService(
    private val platformCacheManager: CacheManager
) {
    @Cacheable("data")
    fun getData(key: String): Data {
        return repository.findByKey(key)
    }
    
    fun updateData(key: String, data: Data) {
        repository.save(data)
        // 분산 캐시 무효화 (Redis Pub/Sub)
        platformCacheManager.evictDistributed("data", key)
    }
}
```

## 🚀 점진적 마이그레이션 전략

### Phase 1: 기반 설정 (1주)
- [ ] Platform Template 의존성 추가
- [ ] DB/Redis/Kafka 설정 통합
- [ ] 기본 Health Check 적용

### Phase 2: 공통 모듈 적용 (2주)
- [ ] 에러 처리를 PlatformException으로 교체
- [ ] API 응답을 ApiResponse로 통일
- [ ] 로깅 시스템 통합

### Phase 3: 실시간 기능 교체 (2주)
- [ ] SSE 알림 시스템 교체
- [ ] Redis Pub/Sub 연동 적용
- [ ] 캐시 무효화 시스템 적용

### Phase 4: 고급 기능 적용 (3주)
- [ ] Kafka 멱등성 보조 적용
- [ ] Outbox 패턴 도입
- [ ] GIS 유틸리티 적용 (해당하는 경우)

## 🔧 호환성 가이드

### Spring Boot 버전
- **지원**: Spring Boot 3.0+
- **권장**: Spring Boot 3.2+
- **Java**: 17+
- **Kotlin**: 1.9+

### 데이터베이스
- **PostgreSQL**: 13+ (PostGIS 3.0+ 권장)
- **Redis**: 6.0+
- **Kafka**: 2.8+

## 🐛 문제 해결

### 자주 발생하는 이슈

#### 1. PostGIS 함수 오류
```
ERROR: function ST_MakeEnvelope(double precision, ...) does not exist
```

**해결책**:
```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

#### 2. Redis 연결 실패
```
Unable to connect to Redis; nested exception is io.lettuce.core.RedisConnectionException
```

**해결책**:
```yaml
spring:
  data:
    redis:
      timeout: 10s
      lettuce:
        pool:
          max-active: 8
          max-wait: -1ms
```

#### 3. Kafka Consumer Group 충돌
```
org.apache.kafka.common.errors.MemberIdRequiredException
```

**해결책**:
```yaml
spring:
  kafka:
    consumer:
      group-id: ${spring.application.name}-${random.uuid}
```

### 성능 최적화

#### 캐시 TTL 조정
```yaml
spring:
  cache:
    redis:
      time-to-live: 1800s  # 30분 (기본 1시간에서 조정)
```

#### Connection Pool 최적화
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      
  data:
    redis:
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 2
```

## 📊 마이그레이션 체크리스트

### 기본 설정
- [ ] Gradle/Maven 의존성 추가
- [ ] application.yml 설정 업데이트
- [ ] 데이터베이스 연결 확인
- [ ] Redis 연결 확인
- [ ] Kafka 연결 확인

### 기능 교체
- [ ] 에러 처리 시스템 교체
- [ ] API 응답 형식 통일
- [ ] SSE 알림 시스템 교체
- [ ] 캐시 시스템 교체
- [ ] 멱등성 처리 적용

### 테스트
- [ ] 단위 테스트 업데이트
- [ ] 통합 테스트 실행
- [ ] 성능 테스트 수행
- [ ] 부하 테스트 (Redis Pub/Sub, SSE)

### 운영 준비
- [ ] 모니터링 설정 (Actuator, Prometheus)
- [ ] 로그 레벨 조정
- [ ] 장애 복구 시나리오 테스트
- [ ] 문서 업데이트

## 🎯 마이그레이션 완료 후 기대 효과

1. **개발 생산성 향상**: 공통 기능 재사용으로 개발 시간 단축
2. **일관성 증대**: 표준화된 에러 처리 및 API 응답
3. **운영 안정성**: 검증된 캐시 무효화, 멱등성 보장
4. **확장성**: Redis Pub/Sub 기반 다중 인스턴스 지원
5. **모니터링**: 표준화된 메트릭 및 헬스 체크

## 🆘 지원

마이그레이션 과정에서 문제가 발생하면:

1. **GitHub Issues**: 버그 리포트 및 기능 요청
2. **Wiki**: 상세한 사용 가이드 및 FAQ
3. **Example Projects**: 실제 마이그레이션 사례

---

마이그레이션이 완료되면 기존 코드 대비 30-50%의 보일러플레이트 코드를 줄일 수 있습니다!