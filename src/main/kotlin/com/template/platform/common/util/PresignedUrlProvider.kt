package com.template.platform.common.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

@Component
class PresignedUrlProvider(
    @Value("\${jwt.secret}")
    private val secret: String
) {
    private val defaultExpiresSeconds = 600L // 10분

    fun createUrl(
        path: String,
        identifier: Long,
        expiresSeconds: Long = defaultExpiresSeconds
    ): String {
        val expires = Instant.now().plusSeconds(expiresSeconds).epochSecond
        val signature = sign("$path|$identifier|$expires")
        return "$path?expires=$expires&signature=$signature"
    }

    fun validate(
        path: String,
        identifier: Long,
        expires: Long,
        signature: String
    ) {
        if (Instant.ofEpochSecond(expires).isBefore(Instant.now())) {
            throw IllegalStateException("Presigned URL expired")
        }
        val expected = sign("$path|$identifier|$expires")
        if (!expected.equals(signature, ignoreCase = true)) {
            throw IllegalStateException("Invalid presigned URL signature")
        }
    }

    private fun sign(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val key = secret.toByteArray(StandardCharsets.UTF_8)
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        val digest = mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}