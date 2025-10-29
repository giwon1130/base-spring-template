package com.template.platform.common.document

import com.template.platform.common.image.ImageUtils
import mu.KotlinLogging
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * Word 문서 문단 빌더
 * 
 * 주요 기능:
 * - 문단 텍스트 스타일링 (폰트, 색상, 정렬)
 * - 이미지 삽입 (자동 리사이징, 크롭)
 * - 문단 간격 및 들여쓰기 제어
 * - 줄 간격 최적화
 * 
 * BMOA 프로젝트에서 이전된 재사용 가능한 Word 문서 생성 유틸리티
 */
class ParagraphBuilder(private val paragraph: XWPFParagraph) {
    
    private val logger = KotlinLogging.logger {}

    // 문단 위/아래 간격 바인딩
    var spacingBefore: Int
        get() = paragraph.spacingBefore
        set(value) { paragraph.spacingBefore = value }

    var spacingAfter: Int
        get() = paragraph.spacingAfter
        set(value) { paragraph.spacingAfter = value }

    /**
     * 문단 여백/줄간격 최소화
     * 이미지가 '텍스트 줄'로 잡히며 생기는 하단 공백 제거용
     */
    private fun compact() {
        paragraph.spacingBefore = 0
        paragraph.spacingAfter = 0
        paragraph.spacingBetween = 1.0
        paragraph.spacingLineRule = LineSpacingRule.EXACT
        paragraph.indentationLeft = 0
        paragraph.indentationRight = 0
    }

    /**
     * 문단에 텍스트 추가
     * 
     * @param content 텍스트 내용
     * @param alignment 문단 정렬 (기본: 왼쪽 정렬)
     * @param fontSize 폰트 크기 (기본: 12)
     * @param color 텍스트 색상 16진수 (기본: "000000")
     * @param fontFamily 폰트 패밀리 (기본: "Pretendard")
     * @param isBold 굵게 여부 (기본: false)
     * @param spacingBefore 문단 앞 간격 (기본: 0)
     * @param indentationLeft 왼쪽 들여쓰기 (기본: 0)
     */
    fun text(
        content: String,
        alignment: ParagraphAlignment = ParagraphAlignment.LEFT,
        fontSize: Int = 12,
        color: String = "000000",
        fontFamily: String = "Pretendard",
        isBold: Boolean = false,
        spacingBefore: Int = 0,
        indentationLeft: Int = 0
    ) {
        compact() // 여백/줄간격 최소화
        paragraph.alignment = alignment
        paragraph.spacingBefore = spacingBefore
        paragraph.indentationLeft = indentationLeft

        val run = paragraph.createRun().apply {
            this.fontFamily = fontFamily
            this.fontSize = fontSize
            this.color = color
            this.isBold = isBold
            setText(content)
            // 불필요한 줄바꿈 금지: addBreak() 호출하지 않음
        }
    }
    
    /**
     * 여러 텍스트 스타일을 하나의 문단에 혼합
     * 
     * @param textBlocks 텍스트 블록 리스트 (각각 다른 스타일 적용 가능)
     */
    fun mixedText(vararg textBlocks: TextBlock) {
        compact()
        
        textBlocks.forEach { block ->
            val run = paragraph.createRun().apply {
                fontFamily = block.fontFamily
                fontSize = block.fontSize
                color = block.color
                isBold = block.isBold
                isItalic = block.isItalic
                setText(block.content)
            }
        }
    }

    /**
     * 지정된 크기로 이미지 삽입 (비율 무시)
     * 
     * @param stream 이미지 입력 스트림
     * @param width 이미지 너비 (포인트)
     * @param height 이미지 높이 (포인트)
     */
    fun image(stream: InputStream, width: Double, height: Double) {
        compact() // 여백/줄간격 최소화
        val run = paragraph.createRun()
        stream.use {
            run.addPicture(
                it,
                XWPFDocument.PICTURE_TYPE_PNG,
                "inline-image.png",
                Units.toEMU(width),
                Units.toEMU(height)
            )
        }
    }
    
    /**
     * BufferedImage를 문단에 삽입
     * 
     * @param image BufferedImage 객체
     * @param width 이미지 너비 (포인트)
     * @param height 이미지 높이 (포인트)
     */
    fun image(image: BufferedImage, width: Double, height: Double) {
        val stream = ImageUtils.bufferedImageToInputStream(image, "PNG")
        image(stream, width, height)
    }

    /**
     * 원본 비율 유지, maxWidth 기준 자동 리사이즈
     * 
     * @param path 이미지 파일 경로
     * @param maxWidth 최대 너비 (포인트)
     */
    fun image(path: String, maxWidth: Double) {
        compact() // 여백/줄간격 최소화
        
        try {
            val imageFile = File(path)
            val imageBytes = imageFile.readBytes()
            val bufferedImage = ImageIO.read(imageFile)

            val width = bufferedImage.width.toDouble()
            val height = bufferedImage.height.toDouble()
            val scale = if (width > maxWidth) maxWidth / width else 1.0
            val finalWidth = width * scale
            val finalHeight = height * scale

            val run = paragraph.createRun()
            run.addPicture(
                ByteArrayInputStream(imageBytes),
                XWPFDocument.PICTURE_TYPE_PNG,
                path,
                Units.toEMU(finalWidth),
                Units.toEMU(finalHeight)
            )
        } catch (e: Exception) {
            logger.error(e) { "이미지 삽입 실패: $path" }
            text("이미지 로드 실패: $path", color = "FF0000", fontSize = 10)
        }
    }

    /**
     * object-fit: cover 느낌으로 중앙 크롭 후 삽입
     * 
     * @param path 이미지 파일 경로
     * @param targetWidth 목표 너비 (포인트)
     * @param targetHeight 목표 높이 (포인트)
     */
    fun imageCropToFit(path: String, targetWidth: Double, targetHeight: Double) {
        compact() // 여백/줄간격 최소화
        
        try {
            val imageFile = File(path)
            val bufferedImage = ImageIO.read(imageFile)
            val imageWidth = bufferedImage.width.toDouble()
            val imageHeight = bufferedImage.height.toDouble()
            val imageRatio = imageWidth / imageHeight
            val targetRatio = targetWidth / targetHeight

            val (cropWidth, cropHeight) = if (imageRatio > targetRatio) {
                val newWidth = (imageHeight * targetRatio).toInt()
                newWidth to imageHeight.toInt()
            } else {
                val newHeight = (imageWidth / targetRatio).toInt()
                imageWidth.toInt() to newHeight
            }

            val x = ((bufferedImage.width - cropWidth) / 2)
            val y = ((bufferedImage.height - cropHeight) / 2)
            val cropped = bufferedImage.getSubimage(x, y, cropWidth, cropHeight)

            val baos = ByteArrayOutputStream()
            ImageIO.write(cropped, "png", baos)

            val run = paragraph.createRun()
            run.addPicture(
                ByteArrayInputStream(baos.toByteArray()),
                XWPFDocument.PICTURE_TYPE_PNG,
                path,
                Units.toEMU(targetWidth),
                Units.toEMU(targetHeight)
            )
        } catch (e: Exception) {
            logger.error(e) { "이미지 크롭 삽입 실패: $path" }
            text("이미지 로드 실패: $path", color = "FF0000", fontSize = 10)
        }
    }
    
    /**
     * 이미지를 자동으로 적절한 크기로 리사이징하여 삽입
     * 
     * @param path 이미지 파일 경로
     * @param maxWidth 최대 너비 (포인트, 기본: 400)
     * @param maxHeight 최대 높이 (포인트, 기본: 300)
     */
    fun imageAutoResize(
        path: String, 
        maxWidth: Double = 400.0, 
        maxHeight: Double = 300.0
    ) {
        try {
            val imageFile = File(path)
            val bufferedImage = ImageIO.read(imageFile)
            
            // 비율 유지하면서 크기 조정
            val resizedImage = ImageUtils.resizeImage(
                bufferedImage, 
                maxWidth.toInt(), 
                maxHeight.toInt()
            )
            
            image(resizedImage, resizedImage.width.toDouble(), resizedImage.height.toDouble())
        } catch (e: Exception) {
            logger.error(e) { "이미지 자동 리사이징 실패: $path" }
            text("이미지 로드 실패: $path", color = "FF0000", fontSize = 10)
        }
    }
    
    /**
     * 문단 정렬 설정
     */
    fun alignment(alignment: ParagraphAlignment) {
        paragraph.alignment = alignment
    }
    
    /**
     * 줄 간격 설정
     */
    fun lineSpacing(spacing: Double, rule: LineSpacingRule = LineSpacingRule.AUTO) {
        paragraph.spacingBetween = spacing
        paragraph.spacingLineRule = rule
    }
    
    /**
     * 들여쓰기 설정
     */
    fun indentation(left: Int = 0, right: Int = 0, firstLine: Int = 0) {
        paragraph.indentationLeft = left
        paragraph.indentationRight = right
        paragraph.indentationFirstLine = firstLine
    }
}

/**
 * 텍스트 블록 데이터 클래스 (mixedText용)
 */
data class TextBlock(
    val content: String,
    val fontSize: Int = 12,
    val color: String = "000000",
    val fontFamily: String = "Pretendard",
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)