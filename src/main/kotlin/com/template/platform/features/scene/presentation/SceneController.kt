package com.template.platform.features.scene.presentation

import com.template.platform.common.response.CommonResponse
import com.template.platform.features.scene.application.SceneFileService
import com.template.platform.features.scene.application.SceneService
import com.template.platform.features.scene.application.dto.SceneCountResponse
import com.template.platform.features.scene.application.dto.SceneRequest
import com.template.platform.features.scene.application.dto.SceneResponse
import com.template.platform.features.scene.domain.SceneStatus
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/scenes")
class SceneController(
    private val sceneService: SceneService,
    private val sceneFileService: SceneFileService
) {

    @GetMapping
    fun getScenes(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdStart: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdEnd: Instant?,
        @RequestParam(required = false) status: List<SceneStatus>?,
        pageable: Pageable
    ): CommonResponse<Page<SceneResponse>> {
        val scenes = sceneService.getSceneList(keyword, createdStart, createdEnd, status, pageable)
        return CommonResponse.success(data = scenes)
    }

    @GetMapping("/{sceneId}")
    fun getScene(@PathVariable sceneId: Long): CommonResponse<SceneResponse> {
        return CommonResponse.success(data = sceneService.getScene(sceneId))
    }

    @PostMapping
    fun createScene(@Valid @RequestBody request: SceneRequest): ResponseEntity<CommonResponse<SceneResponse>> {
        val response = sceneService.createScene(request)
        return ResponseEntity.ok(CommonResponse.success(data = response))
    }

    @PutMapping("/{sceneId}")
    fun updateScene(
        @PathVariable sceneId: Long,
        @Valid @RequestBody request: SceneRequest
    ): CommonResponse<SceneResponse> {
        return CommonResponse.success(data = sceneService.updateScene(sceneId, request))
    }

    @DeleteMapping("/{sceneId}")
    fun deleteScene(@PathVariable sceneId: Long): CommonResponse<Unit> {
        sceneService.deleteScene(sceneId)
        return CommonResponse.success(data = Unit)
    }

    @GetMapping("/count")
    fun countScenes(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdStart: Instant,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) createdEnd: Instant
    ): CommonResponse<SceneCountResponse> {
        return CommonResponse.success(data = sceneService.countScenes(createdStart, createdEnd))
    }

    @GetMapping("/change-detections/{changeDetectionId}/download-url")
    fun getSceneDownloadUrl(@PathVariable changeDetectionId: Long): CommonResponse<String> {
        val url = sceneFileService.generateSceneDownloadUrl(changeDetectionId)
        return CommonResponse.success(data = url)
    }

    @GetMapping("/change-detections/{changeDetectionId}/download")
    fun downloadScene(
        @PathVariable changeDetectionId: Long,
        @RequestParam expires: Long,
        @RequestParam signature: String
    ): ResponseEntity<Resource> {
        val result = sceneFileService.getSceneFile(changeDetectionId, expires, signature)
        return ResponseEntity.ok()
            .contentType(result.mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${result.filename}\"")
            .body(result.resource)
    }

    @GetMapping("/change-detections/{changeDetectionId}/labels/download-url")
    fun getLabelDownloadUrl(@PathVariable changeDetectionId: Long): CommonResponse<String> {
        val url = sceneFileService.generateLabelDownloadUrl(changeDetectionId)
        return CommonResponse.success(data = url)
    }

    @GetMapping("/change-detections/{changeDetectionId}/labels/download")
    fun downloadLabel(
        @PathVariable changeDetectionId: Long,
        @RequestParam expires: Long,
        @RequestParam signature: String
    ): ResponseEntity<Resource> {
        val result = sceneFileService.getLabelFile(changeDetectionId, expires, signature)
        return ResponseEntity.ok()
            .contentType(result.mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${result.filename}\"")
            .body(result.resource)
    }
}
