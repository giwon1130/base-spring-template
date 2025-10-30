package com.template.platform.common.util

import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import com.template.platform.common.response.PageResponse

@Component
class PagingMapper {

    fun <T, R> toPageResponse(
        page: Page<T>,
        mapper: (T) -> R
    ): PageResponse<R> {
        val content = page.content.map(mapper)
        return PageResponse(
            content = content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            page = page.number,
            size = page.size,
            isFirst = page.isFirst,
            isLast = page.isLast
        )
    }
}