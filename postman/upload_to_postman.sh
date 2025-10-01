#!/bin/bash

# Spring Boot Base Template Postman Collection 업로드 스크립트
# 사용법: ./upload_to_postman.sh [POSTMAN_API_KEY]

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Postman API 키 확인
if [ -z "$1" ]; then
    log_error "Postman API 키가 필요합니다."
    echo "사용법: ./upload_to_postman.sh YOUR_POSTMAN_API_KEY"
    echo ""
    echo "💡 Postman API 키 발급 방법:"
    echo "1. Postman 웹사이트 → Settings → API Keys"
    echo "2. Generate API Key 클릭"
    echo "3. 생성된 키를 복사하여 사용"
    exit 1
fi

POSTMAN_API_KEY="$1"

log_info "🚀 Spring Boot Base Template Postman Collection 업로드 시작..."

# 1. 필요한 파일 확인
log_info "필요한 파일 확인 중..."
if [ ! -f "template-postman-collection.json" ]; then
    log_error "template-postman-collection.json 파일이 없습니다."
    log_info "python3 generate_postman_collection.py 명령을 먼저 실행하세요."
    exit 1
fi

if [ ! -f "template-postman-environment.json" ]; then
    log_error "template-postman-environment.json 파일이 없습니다."
    log_info "python3 generate_postman_collection.py 명령을 먼저 실행하세요."
    exit 1
fi

log_success "필요한 파일 확인됨"

# 2. jq 설치 확인
if ! command -v jq &> /dev/null; then
    log_error "jq가 설치되지 않았습니다."
    log_info "macOS: brew install jq"
    log_info "Ubuntu/Debian: sudo apt-get install jq"
    exit 1
fi

# 3. 워크스페이스 목록 조회
log_info "사용 가능한 워크스페이스 조회 중..."
workspaces_response=$(curl -s -X GET \
  "https://api.postman.com/workspaces" \
  -H "X-API-Key: $POSTMAN_API_KEY")

if echo "$workspaces_response" | jq -e '.workspaces' > /dev/null 2>&1; then
    log_success "워크스페이스 목록 조회 성공"
    
    # 첫 번째 워크스페이스 ID 가져오기
    WORKSPACE_ID=$(echo "$workspaces_response" | jq -r '.workspaces[0].id')
    WORKSPACE_NAME=$(echo "$workspaces_response" | jq -r '.workspaces[0].name')
    
    log_info "사용할 워크스페이스: $WORKSPACE_NAME (ID: $WORKSPACE_ID)"
else
    log_error "워크스페이스 조회 실패:"
    echo "$workspaces_response"
    exit 1
fi

# 4. Collection 업로드 준비
log_info "Collection 업로드 준비 중..."
jq -n --argjson collection "$(cat template-postman-collection.json)" '{collection: $collection}' > collection-wrapper.json
log_success "Collection 래퍼 생성됨"

# 5. Collection 업로드
log_info "Collection 업로드 중..."
collection_response=$(curl -s -X POST \
  "https://api.postman.com/collections?workspace=$WORKSPACE_ID" \
  -H "X-API-Key: $POSTMAN_API_KEY" \
  -H 'Content-Type: application/json' \
  -d @collection-wrapper.json)

if echo "$collection_response" | jq -e '.collection.id' > /dev/null 2>&1; then
    COLLECTION_ID=$(echo "$collection_response" | jq -r '.collection.id')
    COLLECTION_NAME=$(echo "$collection_response" | jq -r '.collection.name')
    log_success "Collection 업로드 완료!"
    log_info "Collection ID: $COLLECTION_ID"
    log_info "Collection Name: $COLLECTION_NAME"
else
    log_error "Collection 업로드 실패:"
    echo "$collection_response" | jq '.'
    exit 1
fi

# 6. Environment 업로드 준비
log_info "Environment 업로드 준비 중..."
jq -n --argjson environment "$(cat template-postman-environment.json | jq '.environment')" '{environment: $environment}' > environment-wrapper.json
log_success "Environment 래퍼 생성됨"

# 7. Environment 업로드
log_info "Environment 업로드 중..."
environment_response=$(curl -s -X POST \
  "https://api.postman.com/environments?workspace=$WORKSPACE_ID" \
  -H "X-API-Key: $POSTMAN_API_KEY" \
  -H 'Content-Type: application/json' \
  -d @environment-wrapper.json)

if echo "$environment_response" | jq -e '.environment.id' > /dev/null 2>&1; then
    ENVIRONMENT_ID=$(echo "$environment_response" | jq -r '.environment.id')
    ENVIRONMENT_NAME=$(echo "$environment_response" | jq -r '.environment.name')
    log_success "Environment 업로드 완료!"
    log_info "Environment ID: $ENVIRONMENT_ID"
    log_info "Environment Name: $ENVIRONMENT_NAME"
else
    log_error "Environment 업로드 실패:"
    echo "$environment_response" | jq '.'
    # Collection은 성공했으므로 계속 진행
fi

# 8. 정리
log_info "임시 파일 정리 중..."
rm -f collection-wrapper.json environment-wrapper.json

log_success "🎉 Spring Boot Base Template 업로드 완료!"

echo ""
echo "📋 업로드된 내용:"
echo "📦 Collection: $COLLECTION_NAME"
echo "🌍 Environment: $ENVIRONMENT_NAME"
echo "🏢 Workspace: $WORKSPACE_NAME"
echo ""
echo "✨ 자동화 기능:"
echo "🔐 JWT 토큰 자동 관리"
echo "🔄 토큰 만료 시 자동 재로그인"
echo "🚀 모든 API에 자동 인증 헤더 추가"
echo "🧪 응답 검증 및 상태 체크"
echo ""
echo "🔧 다음 단계:"
echo "1. Postman에서 '$WORKSPACE_NAME' 워크스페이스 확인"
echo "2. Environment를 '$ENVIRONMENT_NAME'로 설정"
echo "3. 템플릿 애플리케이션 실행: ./gradlew bootRun"
echo "4. 회원가입 → 로그인 API 테스트"

# Collection과 Environment ID를 파일에 저장 (향후 업데이트용)
cat > postman-ids.txt << EOF
# Spring Boot Base Template Postman IDs
COLLECTION_ID=$COLLECTION_ID
ENVIRONMENT_ID=$ENVIRONMENT_ID
WORKSPACE_ID=$WORKSPACE_ID
WORKSPACE_NAME=$WORKSPACE_NAME
EOF

log_info "ID 정보가 postman-ids.txt에 저장되었습니다."