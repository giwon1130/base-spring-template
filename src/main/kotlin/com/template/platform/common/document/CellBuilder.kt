package com.template.platform.common.document

import com.template.platform.common.image.ImageUtils
import mu.KotlinLogging
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.*
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import javax.imageio.ImageIO

/**
 * Word 문서 테이블 셀 빌더
 * 
 * 주요 기능:
 * - 셀 텍스트 스타일링 (폰트, 색상, 정렬)
 * - 셀 배경색 설정
 * - 이미지 삽입 (크기 조정, 중앙 정렬, 크롭)
 * - 셀 마진 및 패딩 제어
 * 
 * BMOA 프로젝트에서 이전된 재사용 가능한 Word 문서 생성 유틸리티
 */
class CellBuilder(private val cell: XWPFTableCell) {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * 셀 배경색 설정
     * 
     * @param color 16진수 색상 코드 (예: "FFFFFF", "FF0000")
     */
    fun backgroundColor(color: String) {
        cell.ctTc.addNewTcPr().addNewShd().fill = color
    }
    
    /**
     * 셀에 텍스트 추가
     * 
     * @param content 텍스트 내용
     * @param fontSize 폰트 크기 (기본: 12)
     * @param color 텍스트 색상 16진수 (기본: "000000")
     * @param fontFamily 폰트 패밀리 (기본: "Pretendard")
     * @param isBold 굵게 여부 (기본: false)
     * @param spacingBefore 문단 앞 간격 (기본: 295)
     * @param spacingAfter 문단 뒤 간격 (기본: 0)
     * @param indentationLeft 왼쪽 들여쓰기 (기본: 150)
     * @param align 텍스트 정렬 (기본: 왼쪽 정렬)
     */
    fun text(
        content: String,
        fontSize: Int = 12,
        color: String = "000000",
        fontFamily: String = "Pretendard",
        isBold: Boolean = false,
        spacingBefore: Int = 295,
        spacingAfter: Int = 0,
        indentationLeft: Int = 150,
        align: ParagraphAlignment = ParagraphAlignment.LEFT
    ) {
        // 수직 정렬: 셀 내부 중앙
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER)

        val paragraph = cell.addParagraph()
        paragraph.spacingBefore = spacingBefore
        paragraph.spacingAfter = spacingAfter
        paragraph.indentationLeft = indentationLeft
        paragraph.alignment = align

        val run = paragraph.createRun()
        run.setText(content, 0)
        run.fontFamily = fontFamily
        run.fontSize = fontSize
        run.color = color
        run.isBold = isBold

        // 기본 문단 제거 (깔끔한 레이아웃을 위해)
        if (cell.paragraphs.size > 1) {
            cell.removeParagraph(0)
        }
    }

    /**
     * 셀에 이미지 삽입 (InputStream 사용)
     * 
     * @param stream 이미지 입력 스트림
     * @param width 이미지 너비 (포인트)
     * @param height 이미지 높이 (포인트)
     */
    fun image(stream: InputStream, width: Double, height: Double) {
        val paragraph = cell.addParagraph().apply {
            spacingBefore = 0
            spacingAfter = 0
            alignment = ParagraphAlignment.CENTER
        }

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
        cell.removeParagraph(0)
    }
    
    /**
     * 셀에 이미지 삽입 (BufferedImage 사용)
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
     * 이미지를 셀 중앙에 배치하고 크롭하여 삽입
     * 
     * @param path 이미지 파일 경로
     * @param targetWidth 목표 너비 (픽셀)
     * @param targetHeight 목표 높이 (픽셀)
     * @param backgroundColorHex 배경색 16진수 (기본: "000000")
     */
    fun imageCenteredWithBackground(
        path: String,
        targetWidth: Int,
        targetHeight: Int,
        backgroundColorHex: String = "000000"
    ) {
        try {
            val imageFile = File(path)
            val bufferedImage = ImageIO.read(imageFile)
                ?: throw IllegalArgumentException("이미지를 읽을 수 없습니다: $path")

            val processedImage = processImageForCell(
                bufferedImage, 
                targetWidth, 
                targetHeight, 
                backgroundColorHex,
                cropMode = true
            )
            
            insertProcessedImage(processedImage, targetWidth, targetHeight, backgroundColorHex, path)
        } catch (e: Exception) {
            logger.error(e) { "이미지 삽입 실패: $path" }
            // 실패 시 에러 텍스트 표시
            text("이미지 로드 실패", fontSize = 10, color = "FF0000")
        }
    }

    /**
     * 이미지를 셀 전체 영역에 맞춰 삽입 (비율 무시)
     * 
     * @param path 이미지 파일 경로
     * @param targetWidth 목표 너비 (픽셀)
     * @param targetHeight 목표 높이 (픽셀)
     * @param backgroundColorHex 배경색 16진수 (기본: "000000")
     */
    fun imageFillWholeArea(
        path: String,
        targetWidth: Int,
        targetHeight: Int,
        backgroundColorHex: String = "000000"
    ) {
        try {
            val imageFile = File(path)
            val bufferedImage = ImageIO.read(imageFile)
                ?: throw IllegalArgumentException("이미지를 읽을 수 없습니다: $path")

            val processedImage = processImageForCell(
                bufferedImage, 
                targetWidth, 
                targetHeight, 
                backgroundColorHex,
                cropMode = false
            )
            
            insertProcessedImage(processedImage, targetWidth, targetHeight, backgroundColorHex, path)
        } catch (e: Exception) {
            logger.error(e) { "이미지 삽입 실패: $path" }
            text("이미지 로드 실패", fontSize = 10, color = "FF0000")
        }
    }
    
    /**
     * 이미지 처리 (크롭 또는 스트레치)
     */
    private fun processImageForCell(
        bufferedImage: BufferedImage,
        targetWidth: Int,
        targetHeight: Int,
        backgroundColorHex: String,
        cropMode: Boolean
    ): BufferedImage {
        val bgColor = Color(
            Integer.valueOf(backgroundColorHex.substring(0, 2), 16),
            Integer.valueOf(backgroundColorHex.substring(2, 4), 16),
            Integer.valueOf(backgroundColorHex.substring(4, 6), 16)
        )
        
        val canvas = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        val g = canvas.createGraphics()
        
        try {
            // 배경색 채우기
            g.color = bgColor
            g.fillRect(0, 0, targetWidth, targetHeight)

            if (cropMode) {
                // 크롭 모드: 비율 유지하면서 영역 채우기
                val imageWidth = bufferedImage.width.toDouble()
                val imageHeight = bufferedImage.height.toDouble()
                val targetWidthD = targetWidth.toDouble()
                val targetHeightD = targetHeight.toDouble()

                val widthRatio = targetWidthD / imageWidth
                val heightRatio = targetHeightD / imageHeight
                val ratio = maxOf(widthRatio, heightRatio)

                val scaledWidth = (imageWidth * ratio).toInt()
                val scaledHeight = (imageHeight * ratio).toInt()
                val x = (scaledWidth - targetWidth) / 2
                val y = (scaledHeight - targetHeight) / 2

                val scaledImage = bufferedImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
                g.drawImage(scaledImage, -x, -y, null)
            } else {
                // 채우기 모드: 비율 무시하고 전체 영역 채우기
                g.drawImage(bufferedImage, 0, 0, targetWidth, targetHeight, null)
            }
        } finally {
            g.dispose()
        }
        
        return canvas
    }
    
    /**
     * 처리된 이미지를 셀에 삽입
     */
    private fun insertProcessedImage(
        processedImage: BufferedImage,
        targetWidth: Int,
        targetHeight: Int,
        backgroundColorHex: String,
        originalPath: String
    ) {
        val imageBytes = ImageUtils.bufferedImageToByteArray(processedImage, "PNG")

        // 셀 마진 제거
        val tcPr = cell.ctTc.addNewTcPr()
        val tcMar = tcPr.addNewTcMar()
        listOf(tcMar.addNewTop(), tcMar.addNewBottom(), tcMar.addNewLeft(), tcMar.addNewRight())
            .forEach { it.w = BigInteger.ZERO }

        // 셀 배경색 설정
        cell.color = backgroundColorHex

        // 기존 문단 제거
        cell.removeParagraph(0)

        // 중앙 정렬 문단 생성
        val paragraph = cell.addParagraph().apply {
            alignment = ParagraphAlignment.CENTER
            verticalAlignment = TextAlignment.CENTER
        }

        // 이미지 삽입
        val run = paragraph.createRun()
        ByteArrayInputStream(imageBytes).use {
            run.addPicture(
                it,
                XWPFDocument.PICTURE_TYPE_PNG,
                originalPath,
                Units.toEMU(targetWidth.toDouble()),
                Units.toEMU(targetHeight.toDouble())
            )
        }
    }
    
    /**
     * 셀 마진 설정
     * 
     * @param top 상단 마진
     * @param bottom 하단 마진
     * @param left 좌측 마진
     * @param right 우측 마진
     */
    fun margins(top: Int = 0, bottom: Int = 0, left: Int = 0, right: Int = 0) {
        val tcPr = cell.ctTc.addNewTcPr()
        val tcMar = tcPr.addNewTcMar()
        tcMar.addNewTop().w = BigInteger.valueOf(top.toLong())
        tcMar.addNewBottom().w = BigInteger.valueOf(bottom.toLong())
        tcMar.addNewLeft().w = BigInteger.valueOf(left.toLong())
        tcMar.addNewRight().w = BigInteger.valueOf(right.toLong())
    }
}