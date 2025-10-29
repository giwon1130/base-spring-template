# Spring Boot Base Template

BMOA 프로젝트에서 공통으로 사용하던 인프라 코드를 추려낸 Spring Boot 템플릿입니다. 인증/사용자/SSE/Redis 캐시 등 "플랫폼 공통 기능"까지만 포함하며, 비즈니스 도메인은 이후에 자유롭게 추가할 수 있도록 비워두었습니다.

## 현재 포함 기능

- **인증 & 보안**: Spring Security + JWT (Access/Refresh), Refresh Token Redis 보관
- **사용자 관리**: 회원가입/로그인/내 정보 수정 API, Auditing 포함
- **SSE 알림 골격**: Redis Pub/Sub, Scene/Inference 알림 발행 서비스, 테스트용 엔드포인트
- **Redis 캐시 인프라**: 캐시 이름 상수, 분산 무효화 메시지 발행
- **공통 인프라**: 전역 예외 처리, 표준 응답, Docker Compose(Postgres + Redis), Flyway 마이그레이션

## 기술 스택

| 항목 | 버전/구성 |
|------|-----------|
| Kotlin | 1.9.x |
| Spring Boot | 3.2.1 |
| Java | 17 |
| DB | PostgreSQL (PostGIS) |
| Cache | Redis |
| 빌드 | Gradle Kotlin DSL |
| 데이터베이스 마이그레이션 | Flyway |

## 빠른 시작

### 0. 요구 사항
- Docker & Docker Compose
- JDK 17 (예: Amazon Corretto 17)

### 1. 프로젝트 클론
```bash
git clone <repo-url>
cd spring-boot-base-template
```

### 2. 인프라 서비스 기동
`docker-compose.yml`만으로 Postgres/Redis가 올라가며, 포트는 각각 `35432`, `6380` 입니다.
```bash
docker compose up -d db redis
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

기본적으로 `local` 프로필이 활성화되며, 다음 환경 변수를 통해 동작을 조정할 수 있습니다.

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `local` | 실행 프로필 |
| `JWT_SECRET` | 기본 내장 값 | 운영 환경에서는 반드시 교체 |
| `JWT_EXPIRATION` | `3600000` | Access Token 만료(ms) |

### 4. 종료
```bash
docker compose down
```

## 주요 API 엔드포인트

### 인증 API
| Method | Path | 설명 | 인증 필요 |
|--------|------|------|----------|
| POST | `/api/v1/auth/register` | 회원가입 | ❌ |
| POST | `/api/v1/auth/login` | 로그인 (JWT 발급) | ❌ |
| POST | `/api/v1/auth/refresh` | Access Token 갱신 | ❌ |

### 사용자 API
| Method | Path | 설명 | 인증 필요 |
|--------|------|------|----------|
| GET | `/api/v1/user/me` | 내 정보 조회 | ✅ |
| PUT | `/api/v1/user/me` | 내 정보 수정 | ✅ |

### 알림 API
| Method | Path | 설명 | 인증 필요 |
|--------|------|------|----------|
| GET | `/api/v1/notifications/stream/{email}` | SSE 구독 | ❌ |
| POST | `/api/v1/notifications/test/send` | 테스트 알림 발행 | ❌ |

### 모니터링
| Method | Path | 설명 | 인증 필요 |
|--------|------|------|----------|
| GET | `/actuator/health` | 헬스 체크 | ❌ |
| GET | `/actuator/metrics` | 메트릭 정보 | ❌ |

## API 테스트 예제

### 1. 회원가입
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User",
    "role": "USER"
  }'
```

### 2. 로그인
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

응답 예시:
```json
{
  "status": "SUCCESS",
  "message": "요청이 정상 처리되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "88708aea-5a15-4493-8a96-0cb6ad2afd8a"
  }
}
```

### 3. 인증이 필요한 API 호출
```bash
curl -H "Authorization: Bearer <ACCESS_TOKEN>" \
  http://localhost:8080/api/v1/user/me
```

### 4. Access Token 갱신
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN>"
  }'
```

## 테스트 실행

### 통합 테스트
실제 Postgres/Redis를 사용해 Flyway `clean → migrate` 후 회원가입 로직을 검사합니다.
```bash
SPRING_PROFILES_ACTIVE=local ./gradlew test --tests com.template.platform.features.user.AuthServiceIntegrationTest
```

### 전체 테스트
```bash
./gradlew test
```

### 빌드
```bash
./gradlew build
```

## 데이터베이스 스키마

### Flyway 마이그레이션

| 파일 | 내용 |
|------|------|
| `V1__initial_schema.sql` | PostGIS 확장, Outbox/Changeset 기본 테이블 |
| `V2__create_users_table.sql` | 사용자 테이블 및 감사 컬럼 |

### 주요 테이블

#### users
- `user_id`: Primary Key
- `email`: 이메일 (Unique)
- `password`: 암호화된 비밀번호
- `name`: 사용자 이름
- `role`: 사용자 권한 (USER, ADMIN)
- `created_at`, `updated_at`: 감사 컬럼

## 설정 파일

### application.yml 주요 설정
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:35432/template_db
    username: postgres
    password: postgres
    
  data:
    redis:
      host: localhost
      port: 6380
      key:
        refresh-token: "platform:auth:refresh:"
        
jwt:
  secret: ${JWT_SECRET:기본값}
  expiration: ${JWT_EXPIRATION:3600000}
```

## 보안 설정

### 허용된 엔드포인트
- `/api/v1/auth/**` - 인증 관련
- `/api/v1/notifications/**` - 알림 관련
- `/actuator/**` - 모니터링
- `/swagger-ui/**`, `/v3/api-docs/**` - API 문서

### 인증이 필요한 엔드포인트
- `/api/v1/user/**` - 사용자 정보 관련
- `/api/v1/admin/**` - 관리자 전용 (ADMIN 권한 필요)

## 커스터마이징 가이드

### 1. 패키지/프로젝트 이름 변경
```kotlin
// settings.gradle.kts
rootProject.name = "your-project-name"
```
패키지 이름 `com.template.platform`을 일괄 변경하세요.

### 2. 새 엔티티 추가
- `src/main/resources/db/migration/V3__....sql`에 Flyway 스크립트 작성
- `BaseEntity`/`BaseTimeEntity`를 상속하면 감사 컬럼이 자동 적용됩니다.

### 3. SSE 알림 확장
- `NotificationTargetResolver` 구현을 교체하여 대상자 선택 전략을 커스터마이징할 수 있습니다.

## 아키텍처

### 패키지 구조
```
src/main/kotlin/com/template/platform/
├── bootstrap/           # 핵심 설정 (DB, Redis, Security, Web)
├── common/             # 재사용 가능한 공통 모듈
│   ├── error/          # 표준 에러 체계
│   ├── response/       # API 응답 래퍼
│   ├── sse/           # SSE 알림 시스템
│   ├── cache/         # 캐시 + 무효화
│   └── domain/        # 공통 엔티티 (BaseEntity)
└── features/          # 기능별 모듈
    ├── user/          # 사용자 관리
    └── notification/  # 알림 시스템
```

### 주요 컴포넌트

#### JWT 인증 시스템
- `JwtUtil`: JWT 토큰 생성/검증
- `JwtAuthenticationFilter`: JWT 인증 필터
- `RefreshTokenService`: Refresh Token Redis 관리

#### SSE 알림 시스템
- `SseManager`: SSE 연결 관리
- `RedisNotificationPublisher`: Redis Pub/Sub 발행
- `NotificationService`: 알림 비즈니스 로직

## 다음 단계 제안

- Scene / AOI / Change Detection 등 비즈니스 도메인 이식
- Kafka, 외부 STAC 연동 등 고급 기능
- API 문서화(Swagger) 및 운영 환경 구성

## 개발 노트

현재 버전은 "공통 인프라 템플릿"으로 고정(fix)하기 적합한 상태입니다. 이후 도메인 마이그레이션을 진행하면서 버전을 올리거나 기능을 추가해 나가세요.

## 문제 해결

### 자주 발생하는 이슈

#### 1. Docker 컨테이너가 시작되지 않는 경우
```bash
docker compose down
docker compose up -d db redis
```

#### 2. JWT 관련 오류
- `JWT_SECRET` 환경변수 확인
- 토큰 만료 시간 확인

#### 3. Redis 연결 오류
- Redis 컨테이너 상태 확인: `docker ps`
- 포트 충돌 확인: `lsof -i :6380`

## 라이선스

MIT License

---

**문의사항이나 버그 제보는 GitHub Issues를 활용해 주세요.**