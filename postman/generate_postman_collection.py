#!/usr/bin/env python3
"""
Spring Boot Base Template Postman Collection Generator
템플릿 프로젝트용 Postman 컬렉션 자동 생성 스크립트
"""

import json
import uuid
from datetime import datetime

def create_auth_login_script():
    """로그인 후 access_token 저장하는 스크립트"""
    return {
        "listen": "test",
        "script": {
            "exec": [
                "// 응답 상태 코드 체크",
                "pm.test('응답 상태 코드가 200인지 확인', function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "",
                "// 응답 데이터 파싱",
                "if (pm.response.code === 200) {",
                "    try {",
                "        const jsonData = pm.response.json();",
                "        ",
                "        // CommonResponse 구조 확인",
                "        if (jsonData.success && jsonData.data) {",
                "            const loginData = jsonData.data;",
                "            ",
                "            // access_token 저장",
                "            if (loginData.accessToken) {",
                "                pm.environment.set('access_token', loginData.accessToken);",
                "                console.log('✅ Access Token 저장됨');",
                "            }",
                "            ",
                "            // refresh_token 저장",
                "            if (loginData.refreshToken) {",
                "                pm.environment.set('refresh_token', loginData.refreshToken);",
                "                console.log('✅ Refresh Token 저장됨');",
                "            }",
                "        }",
                "    } catch (e) {",
                "        console.error('❌ 응답 파싱 중 오류:', e);",
                "    }",
                "}"
            ],
            "type": "text/javascript"
        }
    }

def create_auto_login_script():
    """API 호출 전 자동 로그인 스크립트"""
    return {
        "listen": "prerequest",
        "script": {
            "exec": [
                "// JWT 토큰 만료 체크 함수",
                "function isTokenExpired(token) {",
                "    if (!token) return true;",
                "    try {",
                "        const payload = JSON.parse(atob(token.split('.')[1]));",
                "        const currentTime = Math.floor(Date.now() / 1000);",
                "        return payload.exp < currentTime;",
                "    } catch (e) {",
                "        return true;",
                "    }",
                "}",
                "",
                "// 현재 토큰 확인",
                "const currentToken = pm.environment.get('access_token');",
                "",
                "if (!currentToken || isTokenExpired(currentToken)) {",
                "    console.log('🔄 토큰이 없거나 만료됨. 자동 로그인 시도...');",
                "    ",
                "    // 자동 로그인 요청",
                "    const loginRequest = {",
                "        url: pm.environment.get('base_url') + '/auth/login',",
                "        method: 'POST',",
                "        header: {",
                "            'Content-Type': 'application/json'",
                "        },",
                "        body: {",
                "            mode: 'raw',",
                "            raw: JSON.stringify({",
                "                email: pm.environment.get('test_email') || 'test@template.com',",
                "                password: pm.environment.get('test_password') || 'test1234'",
                "            })",
                "        }",
                "    };",
                "    ",
                "    pm.sendRequest(loginRequest, function (err, response) {",
                "        if (err) {",
                "            console.error('❌ 자동 로그인 실패:', err);",
                "            return;",
                "        }",
                "        ",
                "        if (response.code === 200) {",
                "            try {",
                "                const jsonData = response.json();",
                "                if (jsonData.success && jsonData.data && jsonData.data.accessToken) {",
                "                    pm.environment.set('access_token', jsonData.data.accessToken);",
                "                    if (jsonData.data.refreshToken) {",
                "                        pm.environment.set('refresh_token', jsonData.data.refreshToken);",
                "                    }",
                "                    console.log('✅ 자동 로그인 성공');",
                "                } else {",
                "                    console.error('❌ 로그인 응답 형식 오류');",
                "                }",
                "            } catch (e) {",
                "                console.error('❌ 로그인 응답 파싱 오류:', e);",
                "            }",
                "        } else {",
                "            console.error('❌ 로그인 실패. 상태코드:', response.code);",
                "        }",
                "    });",
                "} else {",
                "    console.log('✅ 기존 JWT 토큰 사용 중');",
                "}"
            ],
            "type": "text/javascript"
        }
    }

def create_template_collection():
    """템플릿 프로젝트용 Postman 컬렉션 생성"""
    collection_id = str(uuid.uuid4())
    
    collection = {
        "info": {
            "name": "Spring Boot Base Template API",
            "description": "BMOA 기반 Spring Boot 템플릿 프로젝트 API 컬렉션\n\n## 자동화 기능\n- 로그인 시 자동으로 JWT 토큰 저장\n- 토큰 만료 시 자동 재로그인\n- 모든 인증 필요 API에 자동 토큰 첨부",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
            "_postman_id": collection_id
        },
        "auth": {
            "type": "bearer",
            "bearer": [
                {
                    "key": "token",
                    "value": "{{access_token}}",
                    "type": "string"
                }
            ]
        },
        "event": [
            create_auto_login_script()
        ],
        "item": [
            {
                "name": "🔐 인증 관리",
                "item": [
                    {
                        "name": "회원가입",
                        "request": {
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "email": "test@template.com",
                                    "password": "test1234",
                                    "name": "테스트 사용자",
                                    "role": "USER"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/auth/register",
                                "host": ["{{base_url}}"],
                                "path": ["auth", "register"]
                            },
                            "description": "새로운 사용자 계정을 생성합니다."
                        }
                    },
                    {
                        "name": "로그인",
                        "event": [
                            create_auth_login_script()
                        ],
                        "request": {
                            "auth": {
                                "type": "noauth"
                            },
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "email": "{{test_email}}",
                                    "password": "{{test_password}}"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/auth/login",
                                "host": ["{{base_url}}"],
                                "path": ["auth", "login"]
                            },
                            "description": "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다."
                        }
                    },
                    {
                        "name": "토큰 갱신",
                        "request": {
                            "auth": {
                                "type": "noauth"
                            },
                            "method": "POST",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "refreshToken": "{{refresh_token}}"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/auth/refresh",
                                "host": ["{{base_url}}"],
                                "path": ["auth", "refresh"]
                            },
                            "description": "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다."
                        }
                    }
                ]
            },
            {
                "name": "👤 사용자 관리",
                "item": [
                    {
                        "name": "내 정보 조회",
                        "request": {
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{base_url}}/user/me",
                                "host": ["{{base_url}}"],
                                "path": ["user", "me"]
                            },
                            "description": "현재 로그인한 사용자의 정보를 조회합니다."
                        }
                    },
                    {
                        "name": "내 정보 수정",
                        "request": {
                            "method": "PUT",
                            "header": [
                                {
                                    "key": "Content-Type",
                                    "value": "application/json"
                                }
                            ],
                            "body": {
                                "mode": "raw",
                                "raw": json.dumps({
                                    "name": "수정된 이름"
                                }, indent=2)
                            },
                            "url": {
                                "raw": "{{base_url}}/user/me",
                                "host": ["{{base_url}}"],
                                "path": ["user", "me"]
                            },
                            "description": "현재 로그인한 사용자의 정보를 수정합니다."
                        }
                    }
                ]
            },
            {
                "name": "🏥 헬스체크",
                "item": [
                    {
                        "name": "서버 상태 확인",
                        "request": {
                            "auth": {
                                "type": "noauth"
                            },
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{baseUrl}}/health",
                                "host": ["{{baseUrl}}"],
                                "path": ["health"]
                            },
                            "description": "서버의 상태를 확인합니다. (인증 불필요)"
                        }
                    },
                    {
                        "name": "Actuator Health",
                        "request": {
                            "auth": {
                                "type": "noauth"
                            },
                            "method": "GET",
                            "header": [],
                            "url": {
                                "raw": "{{baseUrl}}/actuator/health",
                                "host": ["{{baseUrl}}"],
                                "path": ["actuator", "health"]
                            },
                            "description": "Spring Boot Actuator 헬스체크를 확인합니다."
                        }
                    }
                ]
            }
        ]
    }
    
    return collection

def create_template_environment():
    """템플릿 프로젝트용 Postman 환경 변수 생성"""
    return {
        "environment": {
            "name": "Spring Boot Base Template",
            "values": [
                {
                    "key": "base_url",
                    "value": "http://localhost:8080/api/v1",
                    "enabled": True,
                    "type": "default",
                    "description": "템플릿 API 베이스 URL"
                },
                {
                    "key": "baseUrl",
                    "value": "http://localhost:8080",
                    "enabled": True,
                    "type": "default",
                    "description": "서버 베이스 URL"
                },
                {
                    "key": "access_token",
                    "value": "",
                    "enabled": True,
                    "type": "secret",
                    "description": "JWT Access Token (자동 설정됨)"
                },
                {
                    "key": "refresh_token",
                    "value": "",
                    "enabled": True,
                    "type": "secret",
                    "description": "JWT Refresh Token (자동 설정됨)"
                },
                {
                    "key": "test_email",
                    "value": "test@template.com",
                    "enabled": True,
                    "type": "default",
                    "description": "테스트용 이메일"
                },
                {
                    "key": "test_password",
                    "value": "test1234",
                    "enabled": True,
                    "type": "secret",
                    "description": "테스트용 비밀번호"
                }
            ]
        }
    }

def main():
    """메인 실행 함수"""
    print("🚀 Spring Boot Base Template Postman 컬렉션 생성 중...")
    
    # 컬렉션 생성
    collection = create_template_collection()
    with open('template-postman-collection.json', 'w', encoding='utf-8') as f:
        json.dump(collection, f, indent=2, ensure_ascii=False)
    
    # 환경 변수 생성
    environment = create_template_environment()
    with open('template-postman-environment.json', 'w', encoding='utf-8') as f:
        json.dump(environment, f, indent=2, ensure_ascii=False)
    
    print("✅ Postman 파일 생성 완료!")
    print("")
    print("📁 생성된 파일:")
    print("  - template-postman-collection.json")
    print("  - template-postman-environment.json")
    print("")
    print("🔧 사용 방법:")
    print("1. Postman 앱에서 Import 클릭")
    print("2. 생성된 JSON 파일들을 가져오기")
    print("3. Environment를 'Spring Boot Base Template'로 설정")
    print("4. 로그인 API 실행하여 토큰 자동 저장 확인")
    print("")
    print("🚨 주의사항:")
    print("- 먼저 애플리케이션을 실행해주세요 (./gradlew bootRun)")
    print("- 테스트용 계정을 먼저 생성해주세요 (회원가입 API 사용)")

if __name__ == "__main__":
    main()