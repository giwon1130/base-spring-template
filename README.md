# Spring Boot Base Template

BMOA 프로젝트에서 추출한 공통 기능들을 포함하는 Spring Boot 기본 템플릿입니다.

## 🚀 포함 기능

### ✅ 인증/인가 시스템
- JWT 기반 Access Token + Refresh Token 관리
- Spring Security 설정
- 사용자 회원가입/로그인/로그아웃 API
- 토큰 갱신 (Refresh Token Rotation)
- 역할 기반 접근 제어 (USER/ADMIN)

### ✅ 사용자 관리
- 사용자 CRUD 기능
- 비밀번호 변경 기능
- 소프트 삭제 지원 (BaseEntity)
- 생성/수정 시간 및 생성자/수정자 추적

### ✅ 관리자 기능
- 사용자 목록 조회 (페이지네이션)
- 사용자 검색 기능
- 사용자 계정 활성화/비활성화
- 관리자용 사용자 정보 수정

### ✅ 인프라 설정
- PostgreSQL + Redis 연동
- Flyway 마이그레이션
- QueryDSL 설정
- Docker Compose 환경

### ✅ 공통 기능
- 전역 예외 처리
- 표준화된 API 응답 포맷 (`CommonResponse`)
- 페이지네이션 유틸리티 (`PageRequest`, `PageResponse`)
- 요청 검증 (Bean Validation)
- 상세한 로깅 및 트레이싱

### ✅ 개발 지원
- Swagger API 문서화
- CORS 설정
- 환경별 설정 (dev/prod)
- 테스트 환경 (TestContainers)

## 🛠️ 기술 스택

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.3.6
- **Database**: PostgreSQL + Redis
- **Security**: Spring Security + JWT
- **Documentation**: SpringDoc OpenAPI 3
- **Build**: Gradle 8.x
- **Java**: 21

## 📋 사용법

### 1. 프로젝트 클론
```bash
git clone https://github.com/giwon1130/spring-boot-base-template.git
cd spring-boot-base-template
```

### 2. 환경 설정
```bash
# 환경 변수 파일 생성
cp .env.example .env

# .env 파일에서 필요한 값들 수정
DB_HOST=localhost
DB_PORT=5432
DB_NAME=templatedb
DB_USERNAME=postgres
DB_PASSWORD=postgres

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=your-base64-encoded-secret-key
```

### 3. Docker 환경 시작
```bash
docker-compose up -d
```

### 4. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 5. API 문서 확인
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## 📱 기본 제공 API

### 인증 API (`/api/v1/auth`)
- `POST /register` - 회원가입
- `POST /login` - 로그인 (JWT 토큰 발급)
- `POST /refresh` - 토큰 갱신 (Refresh Token 사용)
- `POST /logout` - 로그아웃 (토큰 무효화)

### 사용자 관리 API (`/api/v1/user`) 🔒
- `GET /me` - 내 정보 조회
- `PUT /me` - 내 정보 수정
- `POST /change-password` - 비밀번호 변경

### 관리자 API (`/api/v1/admin`) 🔒👑
- `GET /users` - 사용자 목록 조회 (페이지네이션)
- `GET /users/{userId}` - 특정 사용자 정보 조회
- `PUT /users/{userId}` - 사용자 정보 수정
- `DELETE /users/{userId}` - 사용자 계정 비활성화
- `POST /users/{userId}/activate` - 사용자 계정 활성화

### 시스템 API
- `GET /actuator/health` - 헬스체크

> 🔒 = 인증 필요, 👑 = 관리자 권한 필요

## 🔧 커스터마이징

### 1. 프로젝트 이름 변경
```kotlin
// settings.gradle.kts
rootProject.name = "your-project-name"

// 패키지명 변경
com.template.base → com.yourcompany.project
```

### 2. 새로운 엔티티 추가
```kotlin
// 1. domain/model/에 엔티티 생성
@Entity
@Table(name = "your_entity")
class YourEntity : BaseEntity()

// 2. domain/repository/에 리포지토리 생성
interface YourEntityRepository : JpaRepository<YourEntity, Long>

// 3. Flyway 마이그레이션 파일 추가
// src/main/resources/db/migration/V2__create_your_entity_table.sql
```

### 3. 새로운 API 추가
```kotlin
// 1. presentation/controller/에 컨트롤러 생성
@RestController
@RequestMapping("/api/v1/your-entity")
@SecurityRequirement(name = "bearerAuth") // JWT 인증 필요 시
class YourEntityController

// 2. application/service/에 서비스 생성
@Service
@Transactional
class YourEntityService

// 3. DTO 클래스들 생성
// presentation/dto/request/, response/
// 페이지네이션이 필요한 경우 PageRequest, PageResponse 사용
```

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.template.base.*"
```

## 📦 패키지 구조

```
src/main/kotlin/com/template/base/
├── BaseTemplateApplication.kt
├── application/
│   ├── dto/                    # 애플리케이션 레이어 DTO
│   ├── mapper/                 # 엔티티-DTO 매퍼
│   └── service/                # 비즈니스 로직
│       ├── AdminService.kt     # 관리자 기능
│       ├── AuthService.kt      # 회원가입
│       ├── UserService.kt      # 사용자 관리
│       └── auth/              # 인증 관련 서비스
├── domain/
│   ├── model/                  # 엔티티
│   │   ├── common/            # 공통 베이스 엔티티
│   │   └── User.kt
│   └── repository/            # 리포지토리
├── infrastructure/
│   ├── config/                # 설정 클래스들
│   │   ├── SecurityConfig.kt
│   │   ├── RedisConfig.kt
│   │   └── SwaggerConfig.kt
│   └── security/              # 보안 관련
│       ├── JwtUtil.kt
│       └── exception/
└── presentation/
    ├── controller/            # REST 컨트롤러
    │   ├── AdminController.kt  # 관리자 API
    │   ├── AuthController.kt   # 인증 API
    │   ├── HealthController.kt # 헬스체크
    │   └── UserController.kt   # 사용자 API
    └── dto/                   # API 요청/응답 DTO
        ├── request/           # 요청 DTO
        ├── response/          # 응답 DTO
        └── common/            # 공통 DTO (페이지네이션 등)
```

## 🐳 Docker

```bash
# 개발 환경 (PostgreSQL + Redis)
docker-compose up -d

# 전체 빌드 및 실행
docker-compose -f docker-compose.prod.yml up --build
```

## 💡 사용 예제

### 1. 회원가입 및 로그인
```bash
# 회원가입
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "홍길동",
    "role": "USER"
  }'

# 로그인
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 2. 인증이 필요한 API 호출
```bash
# 내 정보 조회
curl -X GET http://localhost:8080/api/v1/user/me \
  -H "Authorization: Bearer {access_token}"

# 비밀번호 변경
curl -X POST http://localhost:8080/api/v1/user/change-password \
  -H "Authorization: Bearer {access_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "password123",
    "newPassword": "newpassword456",
    "confirmPassword": "newpassword456"
  }'
```

### 3. 관리자 API 사용
```bash
# 사용자 목록 조회 (페이지네이션)
curl -X GET "http://localhost:8080/api/v1/admin/users?page=0&size=20&search=홍길동" \
  -H "Authorization: Bearer {admin_access_token}"

# 사용자 계정 비활성화
curl -X DELETE http://localhost:8080/api/v1/admin/users/1 \
  -H "Authorization: Bearer {admin_access_token}"
```

## 📚 참고 문서

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT 사용 가이드](docs/JWT_GUIDE.md)
- [API 설계 가이드](docs/API_DESIGN.md)

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 문의

- GitHub: [@giwon1130](https://github.com/giwon1130)
- Email: your-email@example.com

## 🔄 최신 업데이트

### v2.0.0 (2025-10-13)
- ✅ **관리자 사용자 관리 기능 추가**
  - 사용자 목록 조회 (페이지네이션, 검색)
  - 사용자 계정 활성화/비활성화
  - 관리자용 사용자 정보 수정
- ✅ **로그아웃 기능 구현**
  - Refresh Token 무효화
  - 완전한 세션 종료
- ✅ **비밀번호 변경 기능**
  - 현재 비밀번호 검증
  - 안전한 비밀번호 업데이트
- ✅ **페이지네이션 유틸리티**
  - 표준화된 페이지 요청/응답 구조
  - 정렬 기능 지원
- ✅ **보안 강화**
  - 역할 기반 API 접근 제어
  - JWT 토큰 검증 개선

---

**Based on BMOA Project Architecture** - 검증된 아키텍처를 기반으로 한 안정적이고 확장 가능한 템플릿입니다.