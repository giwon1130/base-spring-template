# Spring Boot Base Template

BMOA 프로젝트에서 검증된 공통 인프라를 추출한 Spring Boot 템플릿입니다. 엔터프라이즈급 인증/보안, 실시간 알림, 공간 데이터 처리, 문서 생성 등 플랫폼 공통 기능을 제공하며, 비즈니스 도메인은 자유롭게 추가할 수 있도록 설계되었습니다.

## 포함된 기능

### 핵심 인프라
- **인증 & 보안**: Spring Security + JWT (Access/Refresh), Redis 기반 세션 관리
- **사용자 관리**: 회원가입/로그인/프로필 관리 API, JPA Auditing 지원
- **실시간 알림**: SSE + Redis Pub/Sub 기반 알림 시스템
- **캐시 시스템**: Redis 분산 캐시, 무효화 메시지 발행
- **전역 예외 처리**: 표준화된 에러 응답, CommonResponse 패턴

### 데이터 처리 모듈 (BMOA 검증 완료)
- **공간 데이터**: PostGIS/JTS 기반 지리정보 처리, WKT/WKB 변환
- **이미지 처리**: 지도 타일 시스템, 그래픽 드로잉, BufferedImage 유틸리티
- **문서 생성**: Apache POI 기반 Word 문서 생성 DSL
- **페이지네이션**: 프론트엔드 친화적인 PageResponse 패턴

### 개발 환경
- **Docker 인프라**: PostgreSQL(PostGIS) + Redis 컨테이너
- **데이터베이스**: Flyway 마이그레이션, 자동 스키마 관리
- **테스트 환경**: TestContainers 기반 통합 테스트 (100% 통과)
- **API 문서**: Swagger/OpenAPI 3.0, JWT 인증 지원

## 기술 스택

| 분야 | 기술 | 버전 |
|------|------|------|
| **언어** | Kotlin | 1.9.x |
| **프레임워크** | Spring Boot | 3.2.1 |
| **JVM** | OpenJDK | 17 |
| **데이터베이스** | PostgreSQL + PostGIS | 15-3.5 |
| **캐시** | Redis | 6.2 |
| **빌드** | Gradle Kotlin DSL | 8.12.1 |
| **ORM** | Spring Data JPA + QueryDSL | 3.2.1 |
| **마이그레이션** | Flyway | 9.x |
| **문서 처리** | Apache POI | 5.2.4 |
| **공간 데이터** | JTS (Java Topology Suite) | - |
| **테스트** | TestContainers + JUnit 5 | - |

## 빠른 시작

### 전제 조건
```bash
# 필수 요구사항
- Docker & Docker Compose
- JDK 17+ (Amazon Corretto 17 권장)
```

### 1. 프로젝트 설정
```bash
git clone <repo-url>
cd spring-boot-base-template
```

### 2. 인프라 실행
```bash
# PostgreSQL + Redis 컨테이너 시작
docker compose up -d db redis

# 컨테이너 상태 확인
docker ps
```

### 3. 애플리케이션 실행
```bash
# 개발 모드 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew build
java -jar build/libs/spring-boot-base-template-*.jar
```

### 4. 상태 확인
```bash
# 헬스 체크
curl http://localhost:8080/actuator/health

# Swagger UI 접속
open http://localhost:8080/swagger-ui/index.html
```

## 환경 설정

### 주요 환경 변수
| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `local` | 실행 프로필 |
| `JWT_SECRET` | 내장값 | JWT 서명 키 (운영환경 필수 변경) |
| `JWT_EXPIRATION` | `3600000` | Access Token 만료시간 (ms) |

### 인프라 설정
| 서비스 | 포트 | 접속 정보 |
|--------|------|----------|
| **애플리케이션** | 8080 | http://localhost:8080 |
| **PostgreSQL** | 35432 | postgres/postgres@localhost:35432/template_db |
| **Redis** | 6380 | localhost:6380 |

## API 가이드

### 표준 응답 형식
모든 API는 일관된 응답 구조를 사용합니다:

```json
{
  "status": "SUCCESS",
  "message": "요청이 정상 처리되었습니다.",
  "data": { ... }
}
```

### 페이지네이션 응답
목록 조회 API는 프론트엔드 친화적인 페이지네이션을 제공합니다:

```json
{
  "status": "SUCCESS",
  "data": {
    "content": [ ... ],
    "totalElements": 100,
    "totalPages": 10,
    "page": 0,
    "size": 10,
    "isFirst": true,
    "isLast": false
  }
}
```

### 핵심 API 엔드포인트

#### 인증 API
```bash
# 회원가입
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "사용자명",
  "role": "USER"
}

# 로그인
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

# 토큰 갱신
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "88708aea-5a15-4493-8a96-0cb6ad2afd8a"
}
```

#### 사용자 API
```bash
# 내 정보 조회
GET /api/v1/user/me
Authorization: Bearer <ACCESS_TOKEN>

# 내 정보 수정
PUT /api/v1/user/me
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "name": "변경된 이름"
}
```

#### Scene/AOI API (페이지네이션 지원)
```bash
# Scene 목록 조회 (필터링 + 페이지네이션)
GET /api/v1/scenes?keyword=test&page=0&size=10&sort=sceneId,desc
Authorization: Bearer <ACCESS_TOKEN>

# AOI 목록 조회 (검색 + 페이지네이션)
GET /api/v1/aois?keyword=seoul&page=0&size=10
Authorization: Bearer <ACCESS_TOKEN>
```

#### 실시간 알림 API
```bash
# SSE 연결
GET /api/v1/notifications/stream/{userEmail}

# 테스트 알림 발송
POST /api/v1/notifications/test/send
```

## 테스트

### 현재 테스트 커버리지: 100% (76/76 통과)

| 모듈 | 테스트 수 | 커버리지 | 주요 기능 |
|------|----------|---------|----------|
| **User Domain** | 6 | 100% | 인증, 사용자 관리 |
| **Common Utils** | 43 | 100% | 공간데이터, 날짜, 파일처리 |
| **Image Processing** | 21 | 100% | 지도타일, 그래픽 렌더링 |
| **Document Generation** | 6 | 100% | Word 문서 생성 |

### 테스트 실행
```bash
# 전체 테스트 (TestContainers 사용, 외부 의존성 불필요)
./gradlew test

# 특정 모듈 테스트
./gradlew test --tests "*User*"
./gradlew test --tests "*GeometryUtils*"
./gradlew test --tests "*PagingMapper*"

# 빌드 + 테스트
./gradlew build
```

### TestContainers 통합 테스트
실제 PostgreSQL(PostGIS) + Redis 컨테이너를 사용하여 프로덕션 환경과 동일한 조건에서 테스트를 수행합니다.

## 아키텍처

### 패키지 구조
```
src/main/kotlin/com/template/platform/
├── bootstrap/              # 핵심 설정
│   ├── config/             # DB, Redis, QueryDSL 설정
│   ├── security/           # JWT, Spring Security
│   └── web/                # Web MVC 설정
├── common/                 # 공통 모듈
│   ├── response/           # API 응답 (CommonResponse, PageResponse)
│   ├── util/               # 유틸리티 (PagingMapper, GeometryUtils)
│   ├── document/           # 문서 생성 (Apache POI DSL)
│   ├── image/              # 이미지 처리 (지도타일, 그래픽)
│   ├── error/              # 전역 예외 처리
│   ├── sse/                # 실시간 알림
│   ├── cache/              # Redis 캐시
│   └── domain/             # 공통 엔티티
└── features/               # 도메인별 기능
    ├── user/               # 사용자 관리
    │   ├── domain/         # User 엔티티
    │   ├── application/    # UserService, AuthService
    │   └── presentation/   # UserController, AuthController
    └── notification/       # 알림 시스템
```

### 주요 설계 패턴

#### 1. CommonResponse 패턴
모든 API가 일관된 응답 구조를 사용합니다:
```kotlin
@GetMapping("/endpoint")
fun getMethod(): CommonResponse<DataType> {
    return CommonResponse.success(data = result)
}
```

#### 2. PageResponse + PagingMapper 패턴
BMOA 검증된 페이지네이션 구조를 사용합니다:
```kotlin
fun getList(pageable: Pageable): PageResponse<ResponseDto> {
    val page = repository.findAll(pageable)
    return pagingMapper.toPageResponse(page) { entity ->
        ResponseDto.from(entity)
    }
}
```

#### 3. DDD 기반 Feature 모듈
도메인별로 독립적인 패키지 구조를 유지합니다:
- `domain/`: 엔티티, 리포지토리
- `application/`: 서비스, DTO
- `presentation/`: 컨트롤러, 요청/응답

## 개발 가이드

### 1. 새로운 도메인 추가
```bash
# 1. 패키지 구조 생성
mkdir -p src/main/kotlin/com/template/platform/features/newdomain/{domain,application,presentation}

# 2. Flyway 마이그레이션 작성
touch src/main/resources/db/migration/V5__create_newdomain_table.sql

# 3. 엔티티 작성 (BaseEntity 상속)
# 4. Repository, Service, Controller 구현
# 5. 테스트 작성
```

### 2. 페이지네이션 적용
```kotlin
// Controller
@GetMapping
fun getItems(pageable: Pageable): CommonResponse<PageResponse<ItemResponse>> {
    return CommonResponse.success(data = service.getItems(pageable))
}

// Service
fun getItems(pageable: Pageable): PageResponse<ItemResponse> {
    val page = repository.findAll(pageable)
    return pagingMapper.toPageResponse(page, ItemResponse::from)
}
```

### 3. 공간 데이터 처리
```kotlin
// WKT를 Polygon으로 변환
val polygon = GeometryUtils.polygonFromWkt("POLYGON((...))")

// 두 지점 간 거리 계산
val distance = GeometryUtils.haversineDistance(lon1, lat1, lon2, lat2)

// BBOX를 Polygon으로 변환
val bbox = GeometryUtils.bboxToPolygon(minX, minY, maxX, maxY)
```

## 운영 환경 배포

### 1. 환경 변수 설정
```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=your-production-secret-key
export DATABASE_URL=jdbc:postgresql://prod-db:5432/app_db
export REDIS_URL=redis://prod-redis:6379
```

### 2. Docker 배포
```dockerfile
FROM openjdk:17-jre-slim
COPY build/libs/spring-boot-base-template-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 3. 헬스 체크
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

## 문제 해결

### 자주 발생하는 이슈

#### 1. 컨테이너 관련
```bash
# 컨테이너 재시작
docker compose down && docker compose up -d db redis

# 로그 확인
docker compose logs db redis

# 포트 충돌 확인
lsof -i :35432 :6380 :8080
```

#### 2. 빌드 관련
```bash
# Gradle 캐시 초기화
./gradlew clean build --refresh-dependencies

# 테스트 제외 빌드
./gradlew build -x test
```

#### 3. 데이터베이스 관련
```bash
# Flyway 정보 확인
./gradlew flywayInfo

# Flyway 재실행
./gradlew flywayClean flywayMigrate
```

## 다음 단계

이 템플릿을 기반으로 다음과 같은 기능을 추가할 수 있습니다:

1. **비즈니스 도메인**: 변화탐지, 객체추론, 보고서 생성
2. **고급 기능**: Kafka, 외부 API 연동, 배치 처리
3. **운영 도구**: 모니터링, 로깅, 배포 자동화
4. **성능 최적화**: 쿼리 튜닝, 캐시 전략, API 최적화

## 기여 가이드

1. Issue 등록 후 브랜치 생성
2. 기능 개발 + 테스트 작성
3. Pull Request 생성
4. 코드 리뷰 후 머지

## 라이선스

MIT License

---

**문의사항**: GitHub Issues 또는 [이메일](mailto:dev@example.com)