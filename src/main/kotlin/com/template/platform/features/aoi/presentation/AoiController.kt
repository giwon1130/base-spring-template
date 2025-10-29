package com.template.platform.features.aoi.presentation

import com.template.platform.common.response.CommonResponse
import com.template.platform.features.aoi.application.AoiService
import com.template.platform.features.aoi.application.dto.AoiRequest
import com.template.platform.features.aoi.application.dto.AoiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/aois")
class AoiController(
    private val aoiService: AoiService
) {

    @GetMapping
    fun getAois(
        @RequestParam(required = false) keyword: String?,
        pageable: Pageable
    ): CommonResponse<Page<AoiResponse>> {
        return CommonResponse.success(data = aoiService.getAoiList(keyword, pageable))
    }

    @GetMapping("/{aoiId}")
    fun getAoi(@PathVariable aoiId: Long): CommonResponse<AoiResponse> {
        return CommonResponse.success(data = aoiService.getAoi(aoiId))
    }

    @PostMapping
    fun createAoi(@Valid @RequestBody request: AoiRequest): CommonResponse<AoiResponse> {
        return CommonResponse.success(data = aoiService.createAoi(request))
    }

    @PutMapping("/{aoiId}")
    fun updateAoi(
        @PathVariable aoiId: Long,
        @Valid @RequestBody request: AoiRequest
    ): CommonResponse<AoiResponse> {
        return CommonResponse.success(data = aoiService.updateAoi(aoiId, request))
    }

    @DeleteMapping("/{aoiId}")
    fun deleteAoi(@PathVariable aoiId: Long): CommonResponse<Unit> {
        aoiService.deleteAoi(aoiId)
        return CommonResponse.success(data = Unit)
    }
}
