# Spring Boot Base Template

인증, 사용자, SSE, Redis 캐시 같은 플랫폼 공통 기능을 먼저 구성해두고 비즈니스 도메인은 이후에 자유롭게 추가할 수 있도록 만든 Spring Boot 템플릿입니다.

## 프로젝트 소개
- 개인 프로젝트나 사내 서비스 초기 구성에서 반복되는 기반 기능을 빠르게 가져가기 위해 만든 템플릿
- 인증, 공통 응답, 예외 처리, 캐시, 알림 같은 인프라 성격 기능을 먼저 포함

## 포함 기능
- Spring Security + JWT 기반 인증
- 회원가입, 로그인, 내 정보 조회/수정
- Redis 기반 Refresh Token 관리
- SSE 알림 골격과 테스트용 엔드포인트
- 공통 예외 처리와 표준 응답 포맷
- Flyway 기반 데이터베이스 마이그레이션
- Docker Compose 기반 로컬 실행 환경
- Gradle Wrapper 포함 및 Testcontainers 기반 통합 테스트

## 기술 스택
- Kotlin 1.9.x
- Spring Boot 3.2.1
- Java 17
- PostgreSQL / PostGIS
- Redis
- Flyway
- Gradle Kotlin DSL

## 사용 목적
- Spring Boot 기반 서비스의 초기 세팅 비용을 줄이고, 반복되는 인프라 코드를 빠르게 시작하기 위한 템플릿

## 사용 예
이 템플릿의 패턴(controller → service → parser → repository 레이어, 공통 응답/예외 포맷, JWT 인증 골격 등)을 따르는 개인 백엔드 라인업:

- `giwon-home-api` — 공개 프로필/프로젝트 카탈로그 API
- `signal-desk-api` — 시장 데이터·합성 위험도 API (PostgreSQL/Railway)
- `baby-log-api` — 육아 기록·실시간 가족 공유·일일 요약 API (PostgreSQL/Railway)
- `codie-api` — 옷장·날씨·TPO 기반 코디 추천 API

## 메모
이 저장소는 비즈니스 로직보다 플랫폼 공통 기능을 빠르게 시작하기 위한 베이스 템플릿 저장소입니다. 신규 백엔드 시작 시 이 패턴을 복사해 도메인만 갈아끼우는 흐름.
