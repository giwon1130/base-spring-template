# BMOA → Spring Boot Template 기능 이전 계획

> **목표**: BMOA 프로젝트의 핵심 기능들을 Spring Boot Base Template으로 체계적으로 이전
> 
> **시작일**: 2025-10-29
> 
> **예상 완료**: 2025-12-31 (약 12-14주)

## 📋 전체 기능 목록 및 이전 상태

### 🎯 Priority 1 - 핵심 인프라 (완료율: 80%)

| 기능 | 설명 | 파일 경로 | 난이도 | 상태 | 완료일 |
|------|------|-----------|--------|------|--------|
| ✅ **공통 응답 형식** | CommonResponse, ErrorCode | `presentation/dto/common/` | Easy | ✅ 완료 | 2025-10-29 |
| ✅ **전역 예외 처리** | GlobalExceptionHandler, CustomException | `infrastructure/security/exception/` | Easy | ✅ 완료 | 2025-10-29 |
| ✅ **Docker 인프라** | PostGIS, Redis 설정 | `docker-compose.yml` | Easy | ✅ 완료 | 2025-10-29 |
| ✅ **SSE 알림 시스템** | 실시간 알림, Redis Pub/Sub | `infrastructure/sse/` | Medium | ✅ 완료 | 2025-10-29 |
| 📋 **JWT 인증/인가** | Spring Security, JWT | `infrastructure/security/` | Medium | 📋 대기 | - |
| 📋 **Redis 캐싱** | Cache Manager, Redis 설정 | `infrastructure/cache/` | Medium | 📋 대기 | - |

### 🎯 Priority 2 - 핵심 도메인 (완료율: 0%)

| 기능 | 설명 | 파일 경로 | 난이도 | 상태 | 완료일 |
|------|------|-----------|--------|------|--------|
| 📋 **사용자 관리** | User 도메인, 회원가입/로그인 | `domain/user/`, `presentation/controller/UserController.kt` | Medium | 📋 대기 | - |
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
- [ ] SSE 알림 시스템
- [ ] JWT 인증/인가
- [ ] Redis 캐싱

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

## ✅ 완료된 작업: SSE 알림 시스템

### 📝 완료된 작업 내용
- ✅ SseManager 구현 (BMOA 호환)
- ✅ RedisNotificationPublisher 구현  
- ✅ NotificationSubscriber 구현
- ✅ NotificationDto 및 도메인 모델 구현
- ✅ NotificationController 업데이트 (BMOA API 호환)
- ✅ NotificationService 구현 (Redis 기반)
- ✅ Redis 설정 업데이트

### 📁 생성된 파일
- `src/main/kotlin/com/template/platform/common/sse/SseManager.kt`
- `src/main/kotlin/com/template/platform/common/sse/RedisNotificationPublisher.kt`
- `src/main/kotlin/com/template/platform/common/sse/NotificationSubscriber.kt`
- `src/main/kotlin/com/template/platform/features/notification/domain/NotificationDto.kt`
- `src/main/kotlin/com/template/platform/features/notification/application/NotificationService.kt`
- `src/main/kotlin/com/template/platform/features/notification/presentation/NotificationController.kt`

## 🔄 다음 작업: JWT 인증/인가 시스템

## 📊 진행률 추적

- **전체 진행률**: 25% (5/20 주요 기능 완료)
- **Priority 1**: 80% (4/5 기능 완료)
- **Priority 2**: 0% (0/6 기능 완료)  
- **Priority 3**: 0% (0/6 기능 완료)

## 🔗 참고 링크

- [BMOA 프로젝트](file:///Users/g/workspace/dev2/BHC/BMOA/BMOA-api-server)
- [Template 프로젝트](file:///Users/g/workspace/dev2/spring-boot-base-template)
- [기존 CLAUDE.md](file:///Users/g/CLAUDE.md)

---

**다음 작업**: JWT 인증/인가 시스템 구현
**예상 소요 시간**: 2-3일  
**담당자**: Claude AI

### 🎯 SSE 알림 시스템 완료!
- Redis Pub/Sub 기반 실시간 알림
- BMOA API 호환 엔드포인트
- 읽음/읽지않음 상태 관리
- Keep-alive 및 연결 관리