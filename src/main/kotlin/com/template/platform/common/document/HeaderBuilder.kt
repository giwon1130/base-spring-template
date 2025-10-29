package com.template.platform.common.document

import org.apache.poi.util.Units
import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFHeader
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import java.io.InputStream

/**
 * Word 문서 헤더 빌더
 * 
 * 주요 기능:
 * - 헤더에 이미지 삽입
 * - 헤더 텍스트 스타일링
 * - 헤더 간격 설정
 * 
 * BMOA 프로젝트에서 이전된 재사용 가능한 Word 문서 생성 유틸리티
 */
class HeaderBuilder(private val document: XWPFDocument) {
    private val header: XWPFHeader = document.createHeader(HeaderFooterType.DEFAULT)
    private val paragraph: XWPFParagraph = header.createParagraph()

    /**
     * 헤더 아래쪽 간격 설정
     */
    var spacingAfter: Int
        get() = paragraph.spacingAfter
        set(value) {
            paragraph.spacingAfter = value
        }

    /**
     * 헤더에 이미지 삽입
     * 
     * @param stream 이미지 입력 스트림
     * @param width 이미지 너비 (포인트)
     * @param height 이미지 높이 (포인트)
     * @param alignment 이미지 정렬 (기본: 중앙 정렬)
     * 
     * 사용 예제:
     * ```kotlin
     * header {
     *     image(logoStream, 150.0, 75.0, ParagraphAlignment.LEFT)
     *     spacingAfter = 500
     * }
     * ```
     */
    fun image(
        stream: InputStream, 
        width: Double, 
        height: Double, 
        alignment: ParagraphAlignment = ParagraphAlignment.CENTER
    ) {
        paragraph.alignment = alignment

        val run = paragraph.createRun()
        stream.use { imageStream ->
            run.addPicture(
                imageStream,
                XWPFDocument.PICTURE_TYPE_PNG,
                "header-image.png",
                Units.toEMU(width),
                Units.toEMU(height)
            )
        }
    }
    
    /**
     * 헤더에 텍스트 추가
     * 
     * @param content 텍스트 내용
     * @param alignment 텍스트 정렬 (기본: 중앙 정렬)
     * @param fontSize 폰트 크기 (기본: 12)
     * @param fontFamily 폰트 패밀리 (기본: "Pretendard")
     * @param isBold 굵게 여부 (기본: false)
     * @param color 텍스트 색상 16진수 (기본: "000000")
     * 
     * 사용 예제:
     * ```kotlin
     * header {
     *     text("회사명", fontSize = 14, isBold = true)
     *     spacingAfter = 300
     * }
     * ```
     */
    fun text(
        content: String,
        alignment: ParagraphAlignment = ParagraphAlignment.CENTER,
        fontSize: Int = 12,
        fontFamily: String = "Pretendard",
        isBold: Boolean = false,
        color: String = "000000"
    ) {
        paragraph.alignment = alignment
        
        val run = paragraph.createRun()
        run.setText(content)
        run.fontFamily = fontFamily
        run.fontSize = fontSize
        run.isBold = isBold
        run.color = color
    }
    
    /**
     * 헤더에 이미지와 텍스트를 함께 추가
     * 
     * @param stream 이미지 입력 스트림
     * @param imageWidth 이미지 너비 (포인트)
     * @param imageHeight 이미지 높이 (포인트)
     * @param text 텍스트 내용
     * @param layout 레이아웃 타입
     * 
     * 사용 예제:
     * ```kotlin
     * header {
     *     imageWithText(
     *         logoStream, 100.0, 50.0,
     *         "회사명",
     *         HeaderLayout.IMAGE_LEFT_TEXT_RIGHT
     *     )
     * }
     * ```
     */
    fun imageWithText(
        stream: InputStream,
        imageWidth: Double,
        imageHeight: Double,
        text: String,
        layout: HeaderLayout = HeaderLayout.IMAGE_LEFT_TEXT_RIGHT,
        fontSize: Int = 12,
        fontFamily: String = "Pretendard",
        isBold: Boolean = false
    ) {
        when (layout) {
            HeaderLayout.IMAGE_LEFT_TEXT_RIGHT -> {
                paragraph.alignment = ParagraphAlignment.LEFT
                
                // 이미지 추가
                val imageRun = paragraph.createRun()
                stream.use { imageStream ->
                    imageRun.addPicture(
                        imageStream,
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "header-image.png",
                        Units.toEMU(imageWidth),
                        Units.toEMU(imageHeight)
                    )
                }
                
                // 간격 추가
                val spaceRun = paragraph.createRun()
                spaceRun.setText("    ") // 4개 공백
                
                // 텍스트 추가
                val textRun = paragraph.createRun()
                textRun.setText(text)
                textRun.fontFamily = fontFamily
                textRun.fontSize = fontSize
                textRun.isBold = isBold
            }
            
            HeaderLayout.IMAGE_TOP_TEXT_BOTTOM -> {
                paragraph.alignment = ParagraphAlignment.CENTER
                
                // 이미지 추가
                val imageRun = paragraph.createRun()
                stream.use { imageStream ->
                    imageRun.addPicture(
                        imageStream,
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "header-image.png",
                        Units.toEMU(imageWidth),
                        Units.toEMU(imageHeight)
                    )
                }
                
                // 줄 바꿈
                val breakRun = paragraph.createRun()
                breakRun.addBreak()
                
                // 텍스트 추가
                val textRun = paragraph.createRun()
                textRun.setText(text)
                textRun.fontFamily = fontFamily
                textRun.fontSize = fontSize
                textRun.isBold = isBold
            }
        }
    }
    
    /**
     * 헤더 정렬 설정
     */
    fun alignment(alignment: ParagraphAlignment) {
        paragraph.alignment = alignment
    }
    
    /**
     * 헤더 간격 설정
     */
    fun spacing(before: Int = 0, after: Int = 0) {
        paragraph.spacingBefore = before
        paragraph.spacingAfter = after
    }
}

/**
 * 헤더 레이아웃 타입
 */
enum class HeaderLayout {
    /** 이미지 왼쪽, 텍스트 오른쪽 */
    IMAGE_LEFT_TEXT_RIGHT,
    
    /** 이미지 위, 텍스트 아래 */
    IMAGE_TOP_TEXT_BOTTOM
}