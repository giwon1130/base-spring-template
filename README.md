# Spring Boot Base Template

BMOA 프로젝트에서 공통으로 사용하던 인프라 코드를 추려낸 Spring Boot 템플릿입니다. 인증/사용자/SSE/Redis 캐시 등 “플랫폼 공통 기능”까지만 포함하며, 비즈니스 도메인은 이후에 자유롭게 추가할 수 있도록 비워두었습니다.

## 🚀 현재 포함 기능

- **인증 & 보안**: Spring Security + JWT (Access/Refresh), Refresh Token Redis 보관
- **사용자 관리**: 회원가입/로그인/내 정보 수정 API, Auditing 포함
- **SSE 알림 골격**: Redis Pub/Sub, Scene/Inference 알림 발행 서비스, 테스트용 엔드포인트
- **Redis 캐시 인프라**: 캐시 이름 상수, 분산 무효화 메시지 발행
- **공통 인프라**: 전역 예외 처리, 표준 응답, Docker Compose(Postgres + Redis), Flyway 마이그레이션

## 🧱 기술 스택

| 항목 | 버전/구성 |
|------|-----------|
| Kotlin | 1.9.x |
| Spring Boot | 3.2.1 |
| Java | 17 |
| DB | PostgreSQL (PostGIS) |
| Cache | Redis |
| 빌드 | Gradle Kotlin DSL |
| 데이터베이스 마이그레이션 | Flyway |

## ⚙️ 빠른 시작

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

### 3. 통합 테스트 (선택)
실제 Postgres/Redis를 사용해 Flyway `clean → migrate` 후 회원가입 로직을 검사합니다.
```bash
SPRING_PROFILES_ACTIVE=local ./gradlew test --tests com.template.platform.features.user.AuthServiceIntegrationTest
```

### 4. 애플리케이션 실행
```bash
./gradlew bootRun
```

기본적으로 `local` 프로필이 활성화되며, 다음 환경 변수를 통해 동작을 조정할 수 있습니다.

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `local` | 실행 프로필 |
| `JWT_SECRET` | 기본 내장 값 | 운영 환경에서는 반드시 교체 |
| `JWT_EXPIRATION` | `3600000` | Access Token 만료(ms) |

### 5. 종료
```bash
docker compose down
```

## 📡 주요 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/auth/register` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 (JWT 발급) |
| POST | `/api/v1/auth/refresh` | Access Token 갱신 |
| GET | `/api/v1/user/me` | 내 정보 조회 (인증 필요) |
| PUT | `/api/v1/user/me` | 내 정보 수정 (인증 필요) |
| GET | `/api/v1/notifications/stream/{email}` | SSE 구독 |
| POST | `/api/v1/notifications/test/send` | 테스트 알림 발행 |

Swagger는 아직 포함되어 있지 않으니 필요 시 직접 추가하세요.

## 🗄️ Flyway 마이그레이션

| 파일 | 내용 |
|------|------|
| `V1__initial_schema.sql` | PostGIS 확장, Outbox/Changeset 기본 테이블 |
| `V2__create_users_table.sql` | 사용자 테이블 및 감사 컬럼 |

## 🧪 테스트 전략

- **통합 테스트**: `AuthServiceIntegrationTest`는 Docker로 띄운 Postgres/Redis(6380)에 대해 Flyway 마이그레이션 후 회원가입 로직을 검증합니다.
- 새 도메인을 이식할 때도 동일한 방식으로 테스트를 추가하면 실제 환경과 거의 동일하게 검증할 수 있습니다.

## 🔧 커스터마이징 팁

1. **패키지/프로젝트 이름 변경**
   ```kotlin
   // settings.gradle.kts
   rootProject.name = "your-project-name"
   ```
   패키지 이름 `com.template.platform`을 일괄 변경하세요.

2. **새 엔티티 추가**
   - `src/main/resources/db/migration/V3__....sql`에 Flyway 스크립트 작성
   - `BaseEntity`/`BaseTimeEntity`를 상속하면 감사 컬럼이 자동 적용됩니다.

3. **SSE 알림 확장**
   - `NotificationTargetResolver` 구현을 교체하여 대상자 선택 전략을 커스터마이징할 수 있습니다.

## 🔭 다음 단계 제안

- Scene / AOI / Change Detection 등 비즈니스 도메인 이식
- Kafka, 외부 STAC 연동 등 고급 기능
- API 문서화(Swagger) 및 운영 환경 구성

현재 버전은 “공통 인프라 템플릿”으로 고정(fix)하기 적합한 상태입니다. 이후 도메인 마이그레이션을 진행하면서 버전을 올리거나 기능을 추가해 나가세요. 버그 제보나 개선 제안은 언제든 환영합니다.
