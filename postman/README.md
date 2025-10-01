# Spring Boot Base Template - Postman Collection

BMOA 기반 Spring Boot 템플릿 프로젝트용 Postman 컬렉션 및 자동화 설정입니다.

## 🚀 빠른 시작

### 1. Postman 컬렉션 생성

```bash
cd postman
python3 generate_postman_collection.py
```

### 2. Postman에서 가져오기

1. Postman 앱 열기
2. **Import** 버튼 클릭
3. 생성된 파일들 드래그 앤 드롭:
   - `template-postman-collection.json`
   - `template-postman-environment.json`

### 3. 환경 설정

1. 오른쪽 상단의 환경 드롭다운에서 **"Spring Boot Base Template"** 선택
2. 환경 변수 확인:
   - `base_url`: `http://localhost:8080/api/v1`
   - `test_email`: `test@template.com` (변경 가능)
   - `test_password`: `test1234` (변경 가능)

## 🔧 사용 방법

### 1. 애플리케이션 실행

```bash
# 템플릿 프로젝트 루트에서
./gradlew bootRun
```

### 2. 테스트 계정 생성

1. **"🔐 인증 관리"** → **"회원가입"** API 실행
2. 환경 변수의 `test_email`, `test_password`와 일치하는 계정 생성

### 3. API 테스트

1. **로그인** API 실행 → 자동으로 JWT 토큰 저장됨
2. **내 정보 조회** API 실행 → 자동으로 인증 헤더 추가됨
3. 다른 인증 필요 API들도 자동으로 토큰이 첨부됨

## ✨ 자동화 기능

### 🔑 자동 로그인
- 토큰이 없거나 만료된 경우 자동으로 로그인 시도
- 모든 인증 필요 API 호출 전에 토큰 유효성 검사

### 🎯 토큰 관리
- 로그인 성공 시 자동으로 `access_token`, `refresh_token` 저장
- JWT 토큰 만료 시간 자동 체크
- 만료 시 자동 재로그인

### 📝 응답 검증
- HTTP 상태 코드 자동 검증
- 응답 데이터 구조 확인
- 콘솔에 상세한 로그 출력

## 📚 API 구성

### 🔐 인증 관리
- **회원가입**: `POST /auth/register`
- **로그인**: `POST /auth/login`
- **토큰 갱신**: `POST /auth/refresh`

### 👤 사용자 관리
- **내 정보 조회**: `GET /user/me`
- **내 정보 수정**: `PUT /user/me`

### 🏥 헬스체크
- **서버 상태 확인**: `GET /health`
- **Actuator Health**: `GET /actuator/health`

## 🛠️ 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `base_url` | `http://localhost:8080/api/v1` | API 베이스 URL |
| `baseUrl` | `http://localhost:8080` | 서버 베이스 URL |
| `access_token` | (자동 설정) | JWT Access Token |
| `refresh_token` | (자동 설정) | JWT Refresh Token |
| `test_email` | `test@template.com` | 테스트용 이메일 |
| `test_password` | `test1234` | 테스트용 비밀번호 |

## 🚨 주의사항

1. **애플리케이션 실행 필수**: Postman 사용 전에 반드시 템플릿 애플리케이션을 실행해주세요.

2. **테스트 계정 생성**: 자동 로그인이 동작하려면 환경 변수의 이메일/비밀번호와 일치하는 계정이 있어야 합니다.

3. **포트 확인**: 애플리케이션이 다른 포트에서 실행되는 경우 `base_url`과 `baseUrl`을 수정해주세요.

4. **토큰 만료**: Access Token 만료 시간은 1시간(기본값)입니다. 자동 갱신 기능이 동작합니다.

## 🔄 업데이트

새로운 API가 추가되면 `generate_postman_collection.py` 스크립트를 수정하고 재실행하여 컬렉션을 업데이트할 수 있습니다.

```bash
python3 generate_postman_collection.py
```

생성된 JSON 파일을 Postman에서 다시 가져오면 업데이트가 완료됩니다.