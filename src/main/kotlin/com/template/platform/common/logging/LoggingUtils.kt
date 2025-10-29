package com.template.platform.common.logging

/**
 * 로깅 관련 유틸리티
 * 
 * 주요 기능:
 * - 민감정보 자동 마스킹 (이메일, 사용자ID, 이름, 전화번호, 파일경로)
 * - 구조화된 로깅 메시지 생성
 * - JSON 응답 민감정보 마스킹
 * - 성능 측정 포맷팅
 */
object LoggingUtils {
    
    /**
     * 이메일 주소 마스킹
     * 
     * @param email 이메일 주소
     * @return 마스킹된 이메일 (예: test@example.com → t***@e******.com)
     */
    fun maskEmail(email: String?): String {
        if (email.isNullOrBlank()) return "***"
        
        val parts = email.split("@")
        if (parts.size != 2) return "***"
        
        val localPart = parts[0]
        val domainPart = parts[1]
        
        val maskedLocal = if (localPart.length <= 1) {
            "*"
        } else {
            "${localPart.first()}${"*".repeat(localPart.length - 1)}"
        }
        
        val maskedDomain = if (domainPart.length <= 1) {
            "*"
        } else {
            "${domainPart.first()}${"*".repeat(maxOf(0, domainPart.length - 2))}${domainPart.last()}"
        }
        
        return "$maskedLocal@$maskedDomain"
    }
    
    /**
     * 사용자 ID 마스킹
     * 
     * @param userId 사용자 ID
     * @return 마스킹된 ID (숫자 ID의 경우 앞 1-2자리만 표시)
     */
    fun maskUserId(userId: Long?): String {
        if (userId == null) return "***"
        val userIdStr = userId.toString()
        return if (userIdStr.length <= 2) {
            "*".repeat(userIdStr.length)
        } else {
            "${userIdStr.substring(0, 2)}${"*".repeat(userIdStr.length - 2)}"
        }
    }
    
    /**
     * 사용자 이름 마스킹
     * 
     * @param name 사용자 이름
     * @return 마스킹된 이름 (한글: 첫 글자만, 영문: 첫/마지막 글자만)
     */
    fun maskUserName(name: String?): String {
        if (name.isNullOrBlank()) return "***"
        
        return when {
            name.length == 1 -> "*"
            name.length == 2 -> "${name.first()}*"
            isKorean(name) -> "${name.first()}${"*".repeat(name.length - 1)}"
            else -> "${name.first()}${"*".repeat(maxOf(0, name.length - 2))}${name.last()}"
        }
    }
    
    /**
     * 전화번호 마스킹
     * 
     * @param phoneNumber 전화번호
     * @return 마스킹된 전화번호 (예: 010-1234-5678 → 010-****-5678)
     */
    fun maskPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return "***"
        
        // 숫자만 추출
        val numbersOnly = phoneNumber.replace(Regex("[^0-9]"), "")
        
        return when {
            numbersOnly.length == 11 -> "${numbersOnly.substring(0, 3)}-****-${numbersOnly.substring(7)}"
            numbersOnly.length == 10 -> "${numbersOnly.substring(0, 3)}-***-${numbersOnly.substring(6)}"
            else -> "***-****-****"
        }
    }
    
    /**
     * 파일 경로에서 민감할 수 있는 부분 마스킹
     * 
     * @param filePath 파일 경로
     * @return 마스킹된 경로 (전체 경로가 아닌 파일명만 표시)
     */
    fun maskFilePath(filePath: String?): String {
        if (filePath.isNullOrBlank()) return "***"
        
        val fileName = filePath.substringAfterLast("/").ifBlank { 
            filePath.substringAfterLast("\\") 
        }
        
        return if (fileName.isBlank()) {
            "***"
        } else {
            "***/$fileName"
        }
    }
    
    /**
     * JSON 응답에서 민감정보 마스킹
     * 
     * @param json JSON 문자열
     * @return 민감정보가 마스킹된 JSON 문자열
     */
    fun maskJsonSensitiveFields(json: String): String {
        return json
            .replace(Regex("\"password\"\\s*:\\s*\"[^\"]*\""), "\"password\":\"***\"")
            .replace(Regex("\"token\"\\s*:\\s*\"[^\"]*\""), "\"token\":\"***\"")
            .replace(Regex("\"secret\"\\s*:\\s*\"[^\"]*\""), "\"secret\":\"***\"")
            .replace(Regex("\"key\"\\s*:\\s*\"[^\"]*\""), "\"key\":\"***\"")
    }
    
    /**
     * 구조화된 로그 메시지 생성
     * 
     * @param action 수행하는 작업 (예: "회원가입", "로그인")
     * @param status 상태 (예: "성공", "실패", "시작")
     * @param details 추가 세부사항 (Map 형태)
     * @return 구조화된 로그 메시지
     */
    fun createStructuredLog(
        action: String,
        status: String,
        details: Map<String, Any?> = emptyMap()
    ): String {
        val detailsStr = details.entries.joinToString(", ") { (key, value) ->
            "$key=$value"
        }
        
        return if (detailsStr.isNotEmpty()) {
            "$action $status - $detailsStr"
        } else {
            "$action $status"
        }
    }
    
    /**
     * 에러 로그용 구조화된 메시지 생성
     * 
     * @param action 수행하려던 작업
     * @param errorType 에러 타입
     * @param errorMessage 에러 메시지
     * @param details 추가 세부사항
     * @return 구조화된 에러 로그 메시지
     */
    fun createErrorLog(
        action: String,
        errorType: String,
        errorMessage: String,
        details: Map<String, Any?> = emptyMap()
    ): String {
        val detailsStr = details.entries.joinToString(", ") { (key, value) ->
            "$key=$value"
        }
        
        return if (detailsStr.isNotEmpty()) {
            "$action 실패 - $errorType: $errorMessage, $detailsStr"
        } else {
            "$action 실패 - $errorType: $errorMessage"
        }
    }
    
    /**
     * 성능 측정 로깅용 포맷터
     * 
     * @param durationMs 소요 시간 (밀리초)
     * @return 포맷된 소요 시간 문자열
     */
    fun formatDuration(durationMs: Long): String {
        return when {
            durationMs < 1000 -> "${durationMs}ms"
            durationMs < 60000 -> String.format("%.2fs", durationMs / 1000.0)
            else -> String.format("%.2fm", durationMs / 60000.0)
        }
    }
    
    /**
     * 한글 문자인지 확인
     * 
     * @param text 확인할 텍스트
     * @return 한글 포함 여부
     */
    private fun isKorean(text: String): Boolean {
        return text.any { char ->
            char in '\uAC00'..'\uD7AF' // 한글 완성형 유니코드 범위
        }
    }
}