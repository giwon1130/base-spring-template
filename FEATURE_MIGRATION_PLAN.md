# BMOA → Spring Boot Template 기능 이전 계획

> **목표**: BMOA 프로젝트의 핵심 기능들을 Spring Boot Base Template으로 체계적으로 이전
> 
> **시작일**: 2025-10-29
> 
> **예상 완료**: 2025-12-31 (약 12-14주)

## 📋 전체 기능 목록 및 이전 상태

### 🎯 Priority 1 - 핵심 인프라 (완료율: 100%)

| 기능 | 설명 | 파일 경로 | 난이도 | 상태 | 완료일 |
|------|------|-----------|--------|------|--------|
| ✅ **공통 응답 형식** | CommonResponse, ErrorCode | `presentation/dto/common/` | Easy | ✅ 완료 | 2025-10-29 |
| ✅ **전역 예외 처리** | GlobalExceptionHandler, CustomException | `infrastructure/security/exception/` | Easy | ✅ 완료 | 2025-10-29 |
| ✅ **Docker 인프라** | PostGIS, Redis 설정 | `docker-compose.yml` | Easy | ✅ 완료 | 2025-10-29 |
| ✅ **SSE 알림 시스템** | 실시간 알림, Redis Pub/Sub | `infrastructure/sse/` | Medium | ✅ 완료 | 2025-10-29 |
| ✅ **JWT 인증/인가** | Spring Security, JWT | `bootstrap/security/`, `features/user/` | Medium | ✅ 완료 | 2025-10-29 |
| ✅ **Redis 캐싱** | Cache Manager, Redis 설정 | `common/cache/`, `features/cache/` | Medium | ✅ 완료 | 2025-10-29 |

### 🎯 Priority 2 - 핵심 도메인 (완료율: 0%)

| 기능 | 설명 | 파일 경로 | 난이도 | 상태 | 완료일 |
|------|------|-----------|--------|------|--------|
| ✅ **사용자 관리** | User 도메인, 회원가입/로그인/프로필 수정 | `features/user/` | Medium | ✅ 완료 | 2025-10-29 |
| 📋 **AOI 관리** | 관심영역 CRUD, 공간데이터 | `domain/aoi/`, `presentation/controller/AoiController.kt` | Hard | 📋 대기 | - |
| 📋 **Scene 관리** | 위성영상 메타데이터 | `domain/scene/`, `presentation/controller/SceneController.kt` | Medium | 📋 대기 | - |
| 📋 **변화 탐지** | AI 변화탐지 로직 | `domain/changedetection/`, `presentation/controller/ChangeDetectionController.kt` | Hard | 📋 대기 | - |
| 📋 **보고서 생성** | PDF 보고서, 파일 관리 | `domain/report/`, `presentation/controller/InitialInterpretationReportController.kt` | Hard | 📋 대기 | - |
| 📋 **객체 추론** | AI 객체 탐지 결과 | `domain/inference/`, `presentation/controller/InferenceObjectController.kt` | Medium | 📋 대기 | - |

### 🎯 Priority 3 - 고급 기능 (완료율: 0%)

| 기능 | 설명 | 파일 경로 | 난이도 | 상태 | 완료일 |
|------|------|-----------|--------|------|--------|
| 📋 **Kafka 메시징** | Event 발행/구독, 비동기 처리 | `infrastructure/kafka/` | Hard | 📋 대기 | - |
| 📋 **STAC API 연동** | 외부 위성영상 데이터 | `infrastructure/external/stac/` | Hard | 📋 대기 | - |
| 📋 **GIS 유틸리티** | PostGIS, JTS 공간연산 | `infrastructure/util/GeometryExtensions.kt` | Hard | 📋 대기 | - |
| 📋 **파일 업로드/다운로드** | S3, 로컬 파일 관리 | `infrastructure/file/` | Medium | 📋 대기 | - |
| 📋 **성능 모니터링** | Micrometer, 트레이싱 | `bootstrap/config/ObservabilityConfig.kt` | Medium | 📋 대기 | - |

## 📅 주차별 실행 계획

### Week 1-2: 핵심 인프라 완성
- [x] 공통 응답 형식 (완료)
- [x] 전역 예외 처리 (완료)
- [x] Docker 인프라 (완료)
- [x] SSE 알림 시스템
- [x] JWT 인증/인가
- [x] Redis 캐싱

### Week 3-4: 기본 도메인 구현
- [ ] 사용자 관리 시스템
- [ ] Scene 관리 (위성영상 메타데이터)

### Week 5-6: 공간 데이터 처리
- [ ] AOI 관리 (PostGIS 연동)
- [ ] GIS 유틸리티 구현

### Week 7-8: AI 처리 시스템
- [ ] 객체 추론 시스템
- [ ] 변화 탐지 기본 구조

### Week 9-10: 메시징 및 외부 연동
- [ ] Kafka 메시징 시스템
- [ ] STAC API 연동

### Week 11-12: 고급 기능 완성
- [ ] 변화 탐지 완성
- [ ] 보고서 생성 시스템

### Week 13-14: 최적화 및 마무리
- [ ] 파일 업로드/다운로드
- [ ] 성능 모니터링
- [ ] 통합 테스트 및 문서화

## ✅ 완료된 작업

### SSE 알림 시스템
- ✅ SseManager, RedisNotificationPublisher, NotificationSubscriber 이식
- ✅ Notification API/DTO/Service 이전 및 Redis Pub/Sub 연동
- ✅ Scene/Inference 알림 발행 로직 및 타깃 리졸버 복구 (`features/notification/application/NotificationService.kt`, `NotificationTargetResolver.kt`)
- ✅ 전 사용자 대상 기본 리졸버 + 알림 VO 추가 (`features/notification/infrastructure/AllUserNotificationTargetResolver.kt`, `features/notification/domain/NotificationPayloads.kt`)

### JWT 인증/인가 시스템
- ✅ Spring Security 필터 체인과 JWT 인증 필터 적용 (`bootstrap/security/SecurityConfig.kt`, `JwtAuthenticationFilter.kt`)
- ✅ JWT 유틸/PasswordEncoder/AuditorAware 구성 (`JwtUtil.kt`, `PasswordEncoderConfig.kt`, `PlatformTemplateApplication.kt`)
- ✅ 사용자 도메인 + Mapper/Service/Controller 마이그레이션 (`features/user/**`)
- ✅ Refresh Token Redis 저장 구조 및 TTL 이식 (`features/user/application/RefreshTokenService.kt`, `application.yml`)

### Redis 캐싱
- ✅ CacheConfig / CacheInvalidationService 이전 (`common/cache/CacheConfig.kt`, `CacheInvalidationService.kt`)
- ✅ CacheManager 연동 및 분산 캐시 무효화 검증

### 사용자 관리
- ✅ 사용자 정보 조회/수정 서비스 및 컨트롤러 이전 (`features/user/application/UserService.kt`, `features/user/presentation/UserController.kt`)
- ✅ UpdateUserRequest DTO 추가 (`features/user/presentation/request/UpdateUserRequest.kt`)
- ✅ UserMapper/Response 재사용 구조 검증
- ✅ 사용자 테이블 Flyway 스키마 추가 (`V2__create_users_table.sql`) 및 통합 테스트 구성 (`AuthServiceIntegrationTest`)

### 📁 생성된 파일
- `src/main/kotlin/com/template/platform/common/sse/SseManager.kt`
- `src/main/kotlin/com/template/platform/common/sse/RedisNotificationPublisher.kt`
- `src/main/kotlin/com/template/platform/common/sse/NotificationSubscriber.kt`
- `src/main/kotlin/com/template/platform/features/notification/domain/NotificationDto.kt`
- `src/main/kotlin/com/template/platform/features/notification/domain/NotificationPayloads.kt`
- `src/main/kotlin/com/template/platform/features/notification/application/NotificationService.kt`
- `src/main/kotlin/com/template/platform/features/notification/application/NotificationTargetResolver.kt`
- `src/main/kotlin/com/template/platform/features/notification/infrastructure/AllUserNotificationTargetResolver.kt`
- `src/main/kotlin/com/template/platform/features/notification/presentation/NotificationController.kt`
- `src/main/kotlin/com/template/platform/bootstrap/security/JwtUtil.kt`
- `src/main/kotlin/com/template/platform/bootstrap/security/JwtAuthenticationFilter.kt`
- `src/main/kotlin/com/template/platform/bootstrap/security/CustomUserDetails.kt`
- `src/main/kotlin/com/template/platform/bootstrap/security/CustomUserDetailsService.kt`
- `src/main/kotlin/com/template/platform/bootstrap/security/PasswordEncoderConfig.kt`
- `src/main/kotlin/com/template/platform/bootstrap/security/SecurityConfig.kt`
- `src/main/kotlin/com/template/platform/common/domain/BaseTimeEntity.kt`
- `src/main/kotlin/com/template/platform/common/domain/BaseEntity.kt`
- `src/main/kotlin/com/template/platform/features/user/**` (도메인, 서비스, DTO, 컨트롤러)
- `src/main/kotlin/com/template/platform/common/cache/CacheConfig.kt`
- `src/main/kotlin/com/template/platform/common/cache/CacheInvalidationService.kt`
- `src/main/kotlin/com/template/platform/features/user/application/UserService.kt`
- `src/main/kotlin/com/template/platform/features/user/presentation/UserController.kt`
- `src/main/kotlin/com/template/platform/features/user/presentation/request/UpdateUserRequest.kt`
- `src/main/resources/db/migration/V2__create_users_table.sql`
- `src/test/kotlin/com/template/platform/features/user/AuthServiceIntegrationTest.kt`

## 🔄 다음 작업: Scene 관리 기능 이전

## 📊 진행률 추적

- **전체 진행률**: 40% (8/20 주요 기능 완료)
- **Priority 1**: 100% (6/6 기능 완료)
- **Priority 2**: 0% (0/6 기능 완료, 사용자 관리 진행중)  
- **Priority 3**: 0% (0/6 기능 완료)

## 🔗 참고 링크

- [BMOA 프로젝트](file:///Users/g/workspace/dev2/BHC/BMOA/BMOA-api-server)
- [Template 프로젝트](file:///Users/g/workspace/dev2/spring-boot-base-template)
- [기존 CLAUDE.md](file:///Users/g/CLAUDE.md)

---

**다음 작업**: Scene 관리 기능 이전  
**예상 소요 시간**: 4일  
**담당자**: Claude AI
