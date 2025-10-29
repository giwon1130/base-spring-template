package com.template.platform.common.cache

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * 캐시 무효화 메시지
 */
data class CacheInvalidationMessage(
    val cacheName: String,
    val key: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): String = mapper.writeValueAsString(this)

    companion object {
        private val mapper = jacksonObjectMapper()

        fun fromJson(json: String): CacheInvalidationMessage =
            mapper.readValue(json)
    }
}
