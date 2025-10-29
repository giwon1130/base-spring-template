package com.template.platform.common.image

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.awt.Color
import java.awt.image.BufferedImage

class ImageUtilsTest {

    @Test
    fun `이미지 포맷 변환 테스트`() {
        // Given: 테스트용 이미지 생성
        val originalImage = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).apply {
            val g = createGraphics()
            g.color = Color.BLUE
            g.fillRect(0, 0, 100, 100)
            g.dispose()
        }

        // When: ByteArray로 변환 후 다시 이미지로 변환
        val byteArray = ImageUtils.bufferedImageToByteArray(originalImage, "PNG")
        val convertedImage = ImageUtils.byteArrayToBufferedImage(byteArray)

        // Then: 변환된 이미지가 null이 아니고 크기가 같아야 함
        assertNotNull(convertedImage)
        assertEquals(originalImage.width, convertedImage!!.width)
        assertEquals(originalImage.height, convertedImage.height)
    }

    @Test
    fun `이미지 리사이징 테스트`() {
        // Given: 원본 이미지 (200x200)
        val originalImage = BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)

        // When: 100x100으로 리사이징
        val resizedImage = ImageUtils.resizeImage(originalImage, 100, 100)

        // Then: 크기가 100x100 이하여야 함 (비율 유지)
        assertTrue(resizedImage.width <= 100)
        assertTrue(resizedImage.height <= 100)
    }

    @Test
    fun `정확한 크기 리사이징 테스트`() {
        // Given: 원본 이미지 (200x200)
        val originalImage = BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)

        // When: 정확히 150x100으로 리사이징
        val resizedImage = ImageUtils.resizeImageExact(originalImage, 150, 100)

        // Then: 정확한 크기여야 함
        assertEquals(150, resizedImage.width)
        assertEquals(100, resizedImage.height)
    }

    @Test
    fun `지원되는 포맷 확인 테스트`() {
        // When: 지원되는 포맷 목록 조회
        val formats = ImageUtils.getSupportedFormats()

        // Then: PNG와 JPEG가 포함되어야 함
        assertTrue(formats.contains("png") || formats.contains("PNG"))
        assertTrue(formats.contains("jpg") || formats.contains("JPG") || 
                   formats.contains("jpeg") || formats.contains("JPEG"))
    }

    @Test
    fun `포맷 유효성 검사 테스트`() {
        // When & Then: 유효한 포맷들
        assertTrue(ImageUtils.isValidFormat("PNG"))
        assertTrue(ImageUtils.isValidFormat("JPEG"))
        assertTrue(ImageUtils.isValidFormat("jpg"))
        
        // When & Then: 유효하지 않은 포맷
        assertFalse(ImageUtils.isValidFormat("INVALID"))
        assertFalse(ImageUtils.isValidFormat(""))
    }
}