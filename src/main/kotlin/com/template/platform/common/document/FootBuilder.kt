package com.template.platform.common.document

import org.apache.poi.util.Units
import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.usermodel.*
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth
import java.io.InputStream
import java.math.BigInteger

/**
 * Word 문서 푸터 빌더
 * 
 * 주요 기능:
 * - 푸터에 텍스트 및 이미지 삽입
 * - 복잡한 테이블 레이아웃 지원
 * - 푸터 정렬 및 간격 설정
 * 
 * BMOA 프로젝트에서 이전된 재사용 가능한 Word 문서 생성 유틸리티
 */
class FootBuilder(private val document: XWPFDocument) {
    private val footer: XWPFFooter = document.createFooter(HeaderFooterType.DEFAULT)
    private val paragraph: XWPFParagraph = footer.createParagraph()
    private val run: XWPFRun = paragraph.createRun()

    init {
        paragraph.alignment = ParagraphAlignment.LEFT // 기본 정렬값
    }

    /**
     * 푸터 아래쪽 간격 설정
     */
    var spacingAfter: Int
        get() = paragraph.spacingAfter
        set(value) {
            paragraph.spacingAfter = value
        }

    /**
     * 푸터에 텍스트 추가
     * 
     * @param content 텍스트 내용
     * @param fontSize 폰트 크기 (기본: 10)
     * @param fontFamily 폰트 패밀리 (기본: "Pretendard")
     * @param isBold 굵게 여부 (기본: false)
     * @param color 텍스트 색상 16진수 (기본: "000000")
     * 
     * 사용 예제:
     * ```kotlin
     * footer {
     *     text("© 2024 회사명. All rights reserved.")
     *     align(ParagraphAlignment.CENTER)
     * }
     * ```
     */
    fun text(
        content: String,
        fontSize: Int = 10,
        fontFamily: String = "Pretendard",
        isBold: Boolean = false,
        color: String = "000000"
    ) {
        run.setText(content)
        run.fontFamily = fontFamily
        run.fontSize = fontSize
        run.isBold = isBold
        run.color = color
    }

    /**
     * 푸터에 이미지 삽입
     * 
     * @param stream 이미지 입력 스트림
     * @param width 이미지 너비 (포인트)
     * @param height 이미지 높이 (포인트)
     * 
     * 사용 예제:
     * ```kotlin
     * footer {
     *     image(logoStream, 80.0, 40.0)
     *     align(ParagraphAlignment.RIGHT)
     * }
     * ```
     */
    fun image(stream: InputStream, width: Double, height: Double) {
        stream.use {
            run.addPicture(
                it,
                XWPFDocument.PICTURE_TYPE_PNG,
                "footer-image.png",
                Units.toEMU(width),
                Units.toEMU(height)
            )
        }
    }

    /**
     * 푸터 정렬 설정
     * 
     * @param alignment 정렬 방식
     */
    fun align(alignment: ParagraphAlignment) {
        paragraph.alignment = alignment
    }
    
    /**
     * 페이지 번호 추가
     * 
     * @param format 페이지 번호 포맷 (기본: "페이지 {pageNum}")
     * @param alignment 정렬 방식 (기본: 중앙 정렬)
     */
    fun pageNumber(
        format: String = "페이지 {pageNum}",
        alignment: ParagraphAlignment = ParagraphAlignment.CENTER
    ) {
        paragraph.alignment = alignment
        
        val parts = format.split("{pageNum}")
        if (parts.isNotEmpty()) {
            run.setText(parts[0])
        }
        
        // 페이지 번호 필드 추가
        paragraph.ctp.addNewFldSimple().instr = "PAGE"
        
        if (parts.size > 1) {
            val endRun = paragraph.createRun()
            endRun.setText(parts[1])
        }
    }

    /**
     * 복잡한 테이블 레이아웃으로 푸터 구성
     * 중앙과 오른쪽에 각각 다른 이미지를 배치
     * 
     * @param centerImageStream 중앙 이미지 스트림
     * @param centerImageSize 중앙 이미지 크기 (너비, 높이)
     * @param rightImageStream 오른쪽 이미지 스트림
     * @param rightImageSize 오른쪽 이미지 크기 (너비, 높이)
     * 
     * 사용 예제:
     * ```kotlin
     * footer {
     *     tableLayoutWithImages(
     *         centerImageStream = logoStream,
     *         centerImageSize = 100.0 to 50.0,
     *         rightImageStream = certStream,
     *         rightImageSize = 80.0 to 40.0
     *     )
     * }
     * ```
     */
    fun tableLayoutWithImages(
        centerImageStream: InputStream,
        centerImageSize: Pair<Double, Double>,
        rightImageStream: InputStream,
        rightImageSize: Pair<Double, Double>
    ) {
        val table = footer.createTable(1, 3)

        // A4 기준 usable width 설정
        val contentWidth = 10600L
        val tblPr = table.ctTbl.tblPr ?: table.ctTbl.addNewTblPr()
        val tblWidth = tblPr.addNewTblW()
        tblWidth.w = BigInteger.valueOf(contentWidth)
        tblWidth.type = STTblWidth.DXA

        // 셀 비율 설정 (왼쪽 공간, 중앙, 오른쪽)
        val columnWidths = listOf(4000L, 2000L, 1000L)
        val row = table.getRow(0)

        columnWidths.forEachIndexed { i, width ->
            val cell = row.getCell(i)
            val tcPr = cell.ctTc.addNewTcPr()
            tcPr.addNewTcW().apply {
                type = STTblWidth.DXA
                w = BigInteger.valueOf(width)
            }

            // 셀 내부 여백 설정
            val cellMargins = tcPr.addNewTcMar()
            cellMargins.addNewLeft().w = when (i) {
                2 -> BigInteger.valueOf(300L) // 오른쪽 셀 왼쪽 여백 추가
                else -> BigInteger.ZERO
            }
            cellMargins.addNewRight().w = BigInteger.ZERO
            cellMargins.addNewTop().w = BigInteger.ZERO
            cellMargins.addNewBottom().w = BigInteger.ZERO

            // 기본 문단 제거
            cell.removeParagraph(0)
        }

        // 1. 왼쪽 셀 (비움)
        row.getCell(0).addParagraph().apply {
            alignment = ParagraphAlignment.LEFT
            spacingBefore = 0
            spacingAfter = 0
            createRun().apply {
                setText("", 0)
                fontSize = 1
            }
        }

        // 2. 가운데 셀 (중앙 이미지)
        row.getCell(1).addParagraph().apply {
            alignment = ParagraphAlignment.CENTER
            spacingBefore = 0
            spacingAfter = 0
            createRun().apply {
                fontSize = 1
                centerImageStream.use {
                    addPicture(
                        it,
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "center.png",
                        Units.toEMU(centerImageSize.first),
                        Units.toEMU(centerImageSize.second)
                    )
                }
            }
        }

        // 3. 오른쪽 셀 (오른쪽 이미지)
        row.getCell(2).addParagraph().apply {
            alignment = ParagraphAlignment.RIGHT
            spacingBefore = 0
            spacingAfter = 0
            createRun().apply {
                fontSize = 1
                rightImageStream.use {
                    addPicture(
                        it,
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "right.png",
                        Units.toEMU(rightImageSize.first),
                        Units.toEMU(rightImageSize.second)
                    )
                }
            }
        }

        // 테두리 제거
        table.removeBorders()
    }
    
    /**
     * 간단한 3분할 레이아웃
     * 
     * @param leftContent 왼쪽 텍스트 (선택적)
     * @param centerContent 중앙 텍스트 (선택적)
     * @param rightContent 오른쪽 텍스트 (선택적)
     */
    fun threeColumnLayout(
        leftContent: String? = null,
        centerContent: String? = null,
        rightContent: String? = null
    ) {
        val table = footer.createTable(1, 3)
        
        // 균등 분할
        val columnWidths = listOf(0.33, 0.34, 0.33)
        val totalWidth = 10600
        
        val row = table.getRow(0)
        columnWidths.forEachIndexed { i, ratio ->
            val cell = row.getCell(i)
            val width = (totalWidth * ratio).toInt()
            
            val tcPr = cell.ctTc.addNewTcPr()
            tcPr.addNewTcW().apply {
                type = STTblWidth.DXA
                w = BigInteger.valueOf(width.toLong())
            }
            
            cell.removeParagraph(0)
        }
        
        // 내용 설정
        val alignments = listOf(
            ParagraphAlignment.LEFT,
            ParagraphAlignment.CENTER,
            ParagraphAlignment.RIGHT
        )
        val contents = listOf(leftContent, centerContent, rightContent)
        
        contents.forEachIndexed { i, content ->
            if (content != null) {
                row.getCell(i).addParagraph().apply {
                    alignment = alignments[i]
                    spacingBefore = 0
                    spacingAfter = 0
                    createRun().apply {
                        setText(content)
                        fontSize = 10
                        fontFamily = "Pretendard"
                    }
                }
            }
        }
        
        table.removeBorders()
    }
    
    /**
     * 푸터 간격 설정
     */
    fun spacing(before: Int = 0, after: Int = 0) {
        paragraph.spacingBefore = before
        paragraph.spacingAfter = after
    }
}