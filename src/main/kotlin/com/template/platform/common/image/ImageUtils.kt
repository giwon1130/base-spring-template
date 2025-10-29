package com.template.platform.common.image

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * 이미지 처리를 위한 공통 유틸리티 클래스
 * 
 * 주요 기능:
 * - 이미지 포맷 변환 (BufferedImage ↔ ByteArray ↔ InputStream)
 * - 이미지 크기 조정 및 리사이징
 * - 이미지 품질 조정
 * 
 * BMOA 프로젝트에서 이전된 재사용 가능한 이미지 처리 함수들
 */
object ImageUtils {
    
    /**
     * BufferedImage를 ByteArray로 변환
     */
    fun bufferedImageToByteArray(image: BufferedImage, format: String = "PNG"): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, format, outputStream)
        return outputStream.toByteArray()
    }
    
    /**
     * BufferedImage를 InputStream으로 변환
     */
    fun bufferedImageToInputStream(image: BufferedImage, format: String = "PNG"): InputStream {
        val byteArray = bufferedImageToByteArray(image, format)
        return ByteArrayInputStream(byteArray)
    }
    
    /**
     * ByteArray를 BufferedImage로 변환
     */
    fun byteArrayToBufferedImage(byteArray: ByteArray): BufferedImage? {
        return try {
            val inputStream = ByteArrayInputStream(byteArray)
            ImageIO.read(inputStream)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * InputStream을 BufferedImage로 변환
     */
    fun inputStreamToBufferedImage(inputStream: InputStream): BufferedImage? {
        return try {
            ImageIO.read(inputStream)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 이미지 크기 조정 (비율 유지)
     * @param image 원본 이미지
     * @param maxWidth 최대 너비
     * @param maxHeight 최대 높이
     * @return 크기 조정된 이미지
     */
    fun resizeImage(image: BufferedImage, maxWidth: Int, maxHeight: Int): BufferedImage {
        val originalWidth = image.width
        val originalHeight = image.height
        
        // 비율 계산
        val widthRatio = maxWidth.toDouble() / originalWidth
        val heightRatio = maxHeight.toDouble() / originalHeight
        val ratio = minOf(widthRatio, heightRatio)
        
        val newWidth = (originalWidth * ratio).toInt()
        val newHeight = (originalHeight * ratio).toInt()
        
        val resizedImage = BufferedImage(newWidth, newHeight, image.type)
        val graphics = resizedImage.createGraphics()
        
        try {
            graphics.drawImage(image, 0, 0, newWidth, newHeight, null)
        } finally {
            graphics.dispose()
        }
        
        return resizedImage
    }
    
    /**
     * 이미지 크기 조정 (정확한 크기)
     * @param image 원본 이미지
     * @param width 새로운 너비
     * @param height 새로운 높이
     * @return 크기 조정된 이미지
     */
    fun resizeImageExact(image: BufferedImage, width: Int, height: Int): BufferedImage {
        val resizedImage = BufferedImage(width, height, image.type)
        val graphics = resizedImage.createGraphics()
        
        try {
            graphics.drawImage(image, 0, 0, width, height, null)
        } finally {
            graphics.dispose()
        }
        
        return resizedImage
    }
    
    /**
     * 지원되는 이미지 포맷 확인
     */
    fun getSupportedFormats(): Array<String> {
        return ImageIO.getWriterFormatNames()
    }
    
    /**
     * 이미지 포맷 검증
     */
    fun isValidFormat(format: String): Boolean {
        return getSupportedFormats().contains(format.uppercase())
    }
}