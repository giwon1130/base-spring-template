package com.template.base.presentation.controller

import com.template.base.presentation.dto.common.CommonResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 헬스체크 및 기본 정보를 제공하는 컨트롤러
 */
@RestController
@Tag(name = "Health API", description = "서버 상태 확인 API")
@RequestMapping("/api/v1/health")
class HealthController {

    @GetMapping
    @Operation(summary = "헬스체크", description = "서버 상태를 확인하는 API (인증 불필요)")
    fun health(): ResponseEntity<CommonResponse<Map<String, String>>> {
        val healthInfo = mapOf(
            "status" to "UP",
            "timestamp" to System.currentTimeMillis().toString(),
            "version" to "1.0.0"
        )
        return ResponseEntity.ok(CommonResponse.success("서버가 정상 동작 중입니다.", healthInfo))
    }
}