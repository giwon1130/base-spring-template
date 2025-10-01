#!/bin/bash

# Spring Boot Base Template API 테스트 스크립트
# 애플리케이션이 실행 중인지 확인하고 기본 API들을 테스트합니다.

BASE_URL="http://localhost:8080"
API_URL="${BASE_URL}/api/v1"

echo "🚀 Spring Boot Base Template API 테스트 시작"
echo "================================="

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 헬스체크
echo -e "\n📋 1. 헬스체크 테스트"
echo "-------------------"

echo -n "서버 상태 확인: "
if curl -s "${BASE_URL}/health" > /dev/null; then
    echo -e "${GREEN}✅ 성공${NC}"
else
    echo -e "${RED}❌ 실패 - 서버가 실행되지 않았거나 응답하지 않습니다.${NC}"
    echo -e "${YELLOW}💡 힌트: './gradlew bootRun' 명령으로 서버를 시작해주세요.${NC}"
    exit 1
fi

echo -n "Actuator 헬스체크: "
if curl -s "${BASE_URL}/actuator/health" > /dev/null; then
    echo -e "${GREEN}✅ 성공${NC}"
else
    echo -e "${YELLOW}⚠️  응답 없음${NC}"
fi

# 회원가입 테스트
echo -e "\n👤 2. 회원가입 테스트"
echo "-------------------"

TEST_EMAIL="test@template.com"
TEST_PASSWORD="test1234"
TEST_NAME="테스트 사용자"

echo -n "회원가입 시도: "
REGISTER_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/register_response.json \
    -X POST "${API_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"${TEST_EMAIL}\",
        \"password\": \"${TEST_PASSWORD}\",
        \"name\": \"${TEST_NAME}\",
        \"role\": \"USER\"
    }")

HTTP_CODE="${REGISTER_RESPONSE: -3}"
if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ 성공${NC}"
elif [ "$HTTP_CODE" -eq 400 ]; then
    # 이미 존재하는 계정일 수 있음
    if grep -q "이미 가입된 이메일" /tmp/register_response.json 2>/dev/null; then
        echo -e "${YELLOW}ℹ️  이미 존재하는 계정 (계속 진행)${NC}"
    else
        echo -e "${RED}❌ 실패 (HTTP: ${HTTP_CODE})${NC}"
        cat /tmp/register_response.json 2>/dev/null || echo ""
    fi
else
    echo -e "${RED}❌ 실패 (HTTP: ${HTTP_CODE})${NC}"
    cat /tmp/register_response.json 2>/dev/null || echo ""
fi

# 로그인 테스트
echo -e "\n🔐 3. 로그인 테스트"
echo "-------------------"

echo -n "로그인 시도: "
LOGIN_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/login_response.json \
    -X POST "${API_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"${TEST_EMAIL}\",
        \"password\": \"${TEST_PASSWORD}\"
    }")

HTTP_CODE="${LOGIN_RESPONSE: -3}"
if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ 성공${NC}"
    
    # 토큰 추출
    ACCESS_TOKEN=$(cat /tmp/login_response.json | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if data.get('success') and data.get('data', {}).get('accessToken'):
        print(data['data']['accessToken'])
except:
    pass
")
    
    if [ -n "$ACCESS_TOKEN" ]; then
        echo -e "   토큰 획득: ${GREEN}✅${NC}"
    else
        echo -e "   토큰 획득: ${RED}❌${NC}"
        echo "   응답: $(cat /tmp/login_response.json)"
    fi
else
    echo -e "${RED}❌ 실패 (HTTP: ${HTTP_CODE})${NC}"
    cat /tmp/login_response.json 2>/dev/null || echo ""
    exit 1
fi

# 인증 필요 API 테스트
echo -e "\n🛡️  4. 인증 필요 API 테스트"
echo "-------------------------"

if [ -n "$ACCESS_TOKEN" ]; then
    echo -n "내 정보 조회: "
    USER_INFO_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/userinfo_response.json \
        -X GET "${API_URL}/user/me" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}")
    
    HTTP_CODE="${USER_INFO_RESPONSE: -3}"
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo -e "${GREEN}✅ 성공${NC}"
        
        # 사용자 정보 확인
        USER_EMAIL=$(cat /tmp/userinfo_response.json | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if data.get('success') and data.get('data', {}).get('email'):
        print(data['data']['email'])
except:
    pass
")
        
        if [ "$USER_EMAIL" = "$TEST_EMAIL" ]; then
            echo -e "   사용자 이메일: ${GREEN}${USER_EMAIL}${NC}"
        else
            echo -e "   사용자 이메일: ${YELLOW}${USER_EMAIL}${NC}"
        fi
    else
        echo -e "${RED}❌ 실패 (HTTP: ${HTTP_CODE})${NC}"
        cat /tmp/userinfo_response.json 2>/dev/null || echo ""
    fi
else
    echo -e "${RED}❌ 토큰이 없어서 테스트 불가${NC}"
fi

# 정리
rm -f /tmp/register_response.json /tmp/login_response.json /tmp/userinfo_response.json

echo -e "\n🎉 테스트 완료!"
echo "================================="
echo -e "${GREEN}✅ 기본 API 동작 확인 완료${NC}"
echo ""
echo "📋 다음 단계:"
echo "1. Postman에서 생성된 컬렉션 가져오기"
echo "2. Environment를 'Spring Boot Base Template'로 설정"
echo "3. 로그인 API로 자동 토큰 저장 테스트"
echo ""
echo "📁 Postman 파일 위치:"
echo "  - template-postman-collection.json"
echo "  - template-postman-environment.json"