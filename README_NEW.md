# Spring Boot Base Template

가벼운 공통 플랫폼 템플릿으로, 기능 중심(feature-first) 아키텍처와 얕은 레이어 적용을 통해 재사용 가능한 베이스를 제공합니다.

## 🎯 주요 기능

### 공통 모듈
- **표준 에러 체계**: 일관된 에러 코드와 응답 형식
- **SSE 알림 시스템**: 단일/다중 인스턴스 지원, Redis Pub/Sub 연동
- **캐시 시스템**: Spring Cache + Redis 분산 무효화
- **Kafka 멱등성 보조**: SETNX 기반 중복 처리 방지
- **Outbox 패턴**: 트랜잭션 보장을 위한 이벤트 발행 골격
- **GIS 유틸리티**: JTS 기반 BBOX, SRID 처리

### 샘플 기능
- **Notification**: SSE 기반 실시간 알림
- **Changeset**: 지리 정보 변경사항 관리 (캐시 적용)

## 🏗️ 기술 스택

- **언어**: Kotlin
- **프레임워크**: Spring Boot 3.2+
- **데이터베이스**: PostgreSQL + PostGIS
- **캐시**: Redis
- **메시징**: Kafka
- **마이그레이션**: Flyway
- **테스트**: TestContainers

## 📁 프로젝트 구조

```
src/main/kotlin/com/template/platform/
├── bootstrap/           # 핵심 설정 (DB, Redis, Kafka, Web)
├── common/             # 재사용 가능한 공통 모듈
│   ├── error/          # 표준 에러 체계
│   ├── response/       # API 응답 래퍼
│   ├── sse/           # SSE 알림 시스템
│   ├── cache/         # 캐시 + 무효화
│   ├── kafka/         # 멱등성 보조
│   ├── outbox/        # Outbox 패턴
│   └── geo/           # GIS 유틸리티
└── features/          # 기능별 모듈
    ├── notification/   # SSE 알림 샘플
    └── changeset/     # 변경사항 관리 샘플
```

## 🚀 빠른 시작

### 1. 환경 준비

```bash
# Docker로 인프라 실행
docker-compose up -d

# 또는 개별 실행
docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgis/postgis:15-3.3
docker run -d --name redis -p 6379:6379 redis:7-alpine
docker run -d --name kafka -p 9092:9092 apache/kafka:latest
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. API 테스트

#### SSE 연결 테스트
```bash
# SSE 스트림 구독
curl -N "http://localhost:8080/sse/subscribe?userId=user1"

# 다른 터미널에서 알림 전송
curl -X POST "http://localhost:8080/sse/send?userId=user1&message=Hello&type=info"

# 브로드캐스트
curl -X POST "http://localhost:8080/sse/broadcast?message=Announcement&type=system"
```

#### 변경사항 조회 테스트
```bash
# BBOX 영역 내 변경사항 조회 (서울 지역)
curl "http://localhost:8080/api/changes?minx=126.9&miny=37.4&maxx=127.1&maxy=37.6"

# 모든 변경사항 조회 (캐시 적용)
curl "http://localhost:8080/api/changes/all"

# 통계 조회
curl "http://localhost:8080/api/changes/statistics"
```

## 🧪 테스트 실행

```bash
# 단위 테스트
./gradlew test

# 통합 테스트 (TestContainers 사용)
./gradlew integrationTest
```

## 📊 모니터링

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Metrics (Prometheus)
```bash
curl http://localhost:8080/actuator/prometheus
```

### API 문서
브라우저에서 http://localhost:8080/swagger-ui.html 접속

## 🔧 설정

### application.yml 주요 설정

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/platform_template
    username: postgres
    password: postgres
    
  data:
    redis:
      host: localhost
      port: 6379
      
  kafka:
    bootstrap-servers: localhost:9092
    
  cache:
    type: redis
    redis:
      time-to-live: 3600s
```

## 📚 주요 컴포넌트 사용법

### 1. SSE 알림 시스템

```kotlin
@Service
class MyService(private val notificationService: NotificationService) {
    
    fun sendAlert(userId: String, message: String) {
        notificationService.sendToUser(userId, "alert", mapOf("message" to message))
    }
    
    fun broadcastMaintenance() {
        notificationService.broadcast("maintenance", mapOf("downtime" to "30min"))
    }
}
```

### 2. 캐시 시스템

```kotlin
@Service
class MyService(private val cacheManager: CacheManager) {
    
    @Cacheable("mydata")
    fun getData(key: String): Data {
        // 비싼 연산
    }
    
    fun invalidateCache(key: String) {
        cacheManager.evictDistributed("mydata", key) // 분산 무효화
    }
}
```

### 3. Kafka 멱등성

```kotlin
@Service
class MyKafkaConsumer(private val idempotencyGuard: IdempotencyGuard) {
    
    @KafkaListener(topics = ["my-topic"])
    fun handleMessage(message: String) {
        val messageId = extractMessageId(message)
        
        idempotencyGuard.executeOnce(messageId) {
            // 실제 비즈니스 로직 (중복 실행 방지됨)
            processMessage(message)
        }
    }
}
```

### 4. GIS 유틸리티

```kotlin
@Service
class GeoService {
    
    fun findNearbyChanges(minX: Double, minY: Double, maxX: Double, maxY: Double): List<Change> {
        val bbox = BBox(minX, minY, maxX, maxY)
        val cacheKey = bbox.toCacheKey("nearby")
        
        return cached(cacheKey) {
            repository.findByBBox(bbox.minX, bbox.minY, bbox.maxX, bbox.maxY)
        }
    }
}
```

## 🚀 기존 프로젝트 마이그레이션

자세한 마이그레이션 가이드는 [MIGRATION.md](MIGRATION.md)를 참조하세요.

## 📄 라이선스

MIT License