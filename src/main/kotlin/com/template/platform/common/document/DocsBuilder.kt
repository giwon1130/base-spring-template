package com.template.platform.common.document

import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.*
import java.math.BigInteger

/**
 * Word 문서 생성을 위한 DSL 빌더 함수
 * 
 * 사용 예제:
 * ```kotlin
 * val document = document {
 *     margins {
 *         top = 1440    // 1인치
 *         bottom = 1440
 *         left = 1440
 *         right = 1440
 *     }
 *     
 *     paragraph {
 *         text("제목", fontSize = 16, isBold = true)
 *     }
 *     
 *     table(rows = 2, cols = 3) {
 *         cell(0, 0) { text("헤더1") }
 *         cell(0, 1) { text("헤더2") }
 *     }
 * }
 * ```
 */
fun document(block: DocsBuilder.() -> Unit): XWPFDocument {
    return DocsBuilder().apply(block).build()
}

/**
 * Word 문서 빌더 클래스
 * 
 * 주요 기능:
 * - 문서 마진 설정
 * - 헤더/푸터 추가
 * - 문단 및 테이블 생성
 * - 페이지 브레이크 및 빈 줄 추가
 * 
 * BMOA 프로젝트에서 이전된 재사용 가능한 Word 문서 생성 유틸리티
 */
class DocsBuilder {
    private val document = XWPFDocument()

    /**
     * 문서 마진 설정
     * 
     * 사용 예제:
     * ```kotlin
     * margins {
     *     top = 1440    // 1인치 (1440 twips)
     *     bottom = 1440
     *     left = 1440
     *     right = 1440
     * }
     * ```
     */
    fun margins(block: Margins.() -> Unit) {
        val margins = Margins().apply(block)
        val sectPr = document.document.body.addNewSectPr()
        val pgMar = sectPr.addNewPgMar()
        pgMar.top = BigInteger.valueOf(margins.top.toLong())
        pgMar.bottom = BigInteger.valueOf(margins.bottom.toLong())
        pgMar.left = BigInteger.valueOf(margins.left.toLong())
        pgMar.right = BigInteger.valueOf(margins.right.toLong())
    }

    /**
     * 문서 헤더 추가
     * 
     * 사용 예제:
     * ```kotlin
     * header {
     *     image(inputStream, 100.0, 50.0, ParagraphAlignment.CENTER)
     *     spacingAfter = 300
     * }
     * ```
     */
    fun header(block: HeaderBuilder.() -> Unit) {
        HeaderBuilder(document).apply(block)
    }

    /**
     * 문서 푸터 추가
     * 
     * 사용 예제:
     * ```kotlin
     * footer {
     *     text("페이지 하단 텍스트")
     *     align(ParagraphAlignment.CENTER)
     * }
     * ```
     */
    fun footer(builder: FootBuilder.() -> Unit) {
        FootBuilder(document).apply(builder)
    }

    /**
     * 문단 추가
     * 
     * @param alignment 문단 정렬 (기본: 왼쪽 정렬)
     * @param block 문단 빌더 블록
     * 
     * 사용 예제:
     * ```kotlin
     * paragraph(ParagraphAlignment.CENTER) {
     *     text("중앙 정렬된 제목", fontSize = 16, isBold = true)
     * }
     * ```
     */
    fun paragraph(
        alignment: ParagraphAlignment = ParagraphAlignment.LEFT,
        block: ParagraphBuilder.() -> Unit
    ) {
        val paragraph = document.createParagraph()
        paragraph.alignment = alignment
        ParagraphBuilder(paragraph).apply(block)
    }

    /**
     * 테이블 추가
     * 
     * @param rows 행 수
     * @param cols 열 수
     * @param columnWidths 열 너비 비율 리스트 (선택적)
     * @param block 테이블 빌더 블록
     * 
     * 사용 예제:
     * ```kotlin
     * table(rows = 2, cols = 3, columnWidths = listOf(0.3, 0.4, 0.3)) {
     *     cell(0, 0) { text("헤더1", isBold = true) }
     *     cell(0, 1) { text("헤더2", isBold = true) }
     *     cell(1, 0) { text("데이터1") }
     * }
     * ```
     */
    fun table(rows: Int, cols: Int, columnWidths: List<Double>? = null, block: TableBuilder.() -> Unit) {
        val table = document.createTable(rows, cols)
        TableBuilder(table, columnWidths).apply(block)
    }

    /**
     * 빈 줄 추가
     * 
     * @param count 빈 줄 개수 (기본: 1)
     * @param fontSize 폰트 크기 (기본: 1, 최대한 작은 크기)
     * @param spacingBefore 위쪽 간격 (포인트, 기본: 0.0)
     * @param spacingAfter 아래쪽 간격 (포인트, 기본: 0.0)
     * 
     * 사용 예제:
     * ```kotlin
     * emptyLine(count = 2, spacingAfter = 12.0)  // 2줄 빈 줄, 아래 12pt 간격
     * ```
     */
    fun emptyLine(
        count: Int = 1,
        fontSize: Int = 1,
        spacingBefore: Double = 0.0,
        spacingAfter: Double = 0.0
    ) {
        repeat(count) {
            val paragraph = document.createParagraph()
            paragraph.spacingBefore = Units.toEMU(spacingBefore)
            paragraph.spacingAfter = Units.toEMU(spacingAfter)

            val run = paragraph.createRun()
            run.setText(" ") // 빈 줄을 넣기 위해 공백 추가
            run.fontSize = fontSize
        }
    }

    /**
     * 페이지 브레이크 추가 - 다음 페이지로 넘어감
     * 
     * 사용 예제:
     * ```kotlin
     * pageBreak()  // 새 페이지 시작
     * ```
     */
    fun pageBreak() {
        val paragraph = document.createParagraph()
        val run = paragraph.createRun()
        run.addBreak(BreakType.PAGE)
    }
    
    /**
     * 콜럼 브레이크 추가 (다중 컬럼 문서에서 사용)
     */
    fun columnBreak() {
        val paragraph = document.createParagraph()
        val run = paragraph.createRun()
        run.addBreak(BreakType.COLUMN)
    }
    
    /**
     * 줄 바꿈 추가
     */
    fun lineBreak() {
        val paragraph = document.createParagraph()
        val run = paragraph.createRun()
        run.addBreak(BreakType.TEXT_WRAPPING)
    }
    
    /**
     * 제목 문단 추가 (미리 정의된 스타일)
     * 
     * @param content 제목 텍스트
     * @param level 제목 레벨 (1-6, 기본: 1)
     * @param alignment 정렬 (기본: 왼쪽 정렬)
     */
    fun heading(
        content: String, 
        level: Int = 1, 
        alignment: ParagraphAlignment = ParagraphAlignment.LEFT
    ) {
        paragraph(alignment) {
            val fontSize = when (level) {
                1 -> 18
                2 -> 16
                3 -> 14
                4 -> 13
                5 -> 12
                6 -> 11
                else -> 12
            }
            text(content, fontSize = fontSize, isBold = true)
        }
    }
    
    /**
     * 구분선 추가 (수평선)
     */
    fun horizontalLine() {
        paragraph {
            text("_".repeat(50), fontSize = 8, color = "CCCCCC")
        }
    }

    /**
     * 문서 빌드 완료
     */
    fun build(): XWPFDocument = document
}

/**
 * 문서 마진 설정 클래스
 * 
 * 단위: twips (1/20 포인트)
 * - 1인치 = 1440 twips
 * - 1cm = 567 twips
 */
class Margins {
    /** 상단 마진 (twips) */
    var top: Int = 0
    
    /** 하단 마진 (twips) */
    var bottom: Int = 0
    
    /** 좌측 마진 (twips) */
    var left: Int = 0
    
    /** 우측 마진 (twips) */
    var right: Int = 0
    
    /**
     * 모든 마진을 동일하게 설정
     */
    fun all(margin: Int) {
        top = margin
        bottom = margin
        left = margin
        right = margin
    }
    
    /**
     * 인치 단위로 마진 설정
     */
    fun allInches(inches: Double) {
        val twips = (inches * 1440).toInt()
        all(twips)
    }
    
    /**
     * 센티미터 단위로 마진 설정
     */
    fun allCentimeters(cm: Double) {
        val twips = (cm * 567).toInt()
        all(twips)
    }
}