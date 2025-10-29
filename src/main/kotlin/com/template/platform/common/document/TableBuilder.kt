package com.template.platform.common.document

import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*
import java.math.BigInteger

/**
 * Word 문서 테이블 빌더
 * 
 * 주요 기능:
 * - 셀별 개별 스타일링
 * - 열 너비 자동 조정
 * - 테이블 테두리 설정
 * - 셀 하이라이트
 * 
 * BMOA 프로젝트에서 이전된 재사용 가능한 Word 문서 생성 유틸리티
 */
class TableBuilder(private val table: XWPFTable, columnWidths: List<Double>?) {
    
    init {
        setupColumnWidths(columnWidths)
    }
    
    /**
     * 열 너비 설정
     */
    private fun setupColumnWidths(columnWidths: List<Double>?) {
        val totalWidth = 12000  // Word 문서의 표준 너비
        val numOfColumns = table.getRow(0)?.tableCells?.size ?: 0
        
        val adjustedWidths = columnWidths?.map { (it * totalWidth).toInt() }
            ?: List(numOfColumns) { totalWidth / numOfColumns }

        for (rowIndex in 0 until table.numberOfRows) {
            val row = table.getRow(rowIndex)
            for (colIndex in 0 until numOfColumns) {
                val cell = row.getCell(colIndex)
                setCellWidth(cell, adjustedWidths[colIndex])
            }
        }
    }

    /**
     * 특정 셀에 대한 작업 수행
     * 
     * @param row 행 인덱스 (0부터 시작)
     * @param col 열 인덱스 (0부터 시작)
     * @param block 셀 빌더 블록
     * 
     * 사용 예제:
     * ```kotlin
     * cell(0, 0) {
     *     text("헤더", isBold = true, backgroundColor = "EEEEEE")
     *     backgroundColor("F0F0F0")
     * }
     * ```
     */
    fun cell(row: Int, col: Int, block: CellBuilder.() -> Unit) {
        if (row >= table.numberOfRows || col >= table.getRow(row).tableCells.size) {
            throw IndexOutOfBoundsException("셀 인덱스 범위 초과: ($row, $col)")
        }
        
        val cell = table.getRow(row).getCell(col)
        CellBuilder(cell).apply(block)
    }

    /**
     * 셀 너비 설정 (내부 함수)
     */
    private fun setCellWidth(cell: XWPFTableCell, width: Int) {
        val cellCT = cell.ctTc.addNewTcPr().addNewTcW()
        cellCT.type = STTblWidth.DXA
        cellCT.w = width
    }

    /**
     * 여러 셀을 동시에 하이라이트
     * 
     * @param cells 하이라이트할 셀들의 (행, 열) 좌표 쌍
     * @param color 배경색 16진수 코드
     * 
     * 사용 예제:
     * ```kotlin
     * highlightCells(
     *     Pair(0, 0), Pair(0, 1), Pair(0, 2),  // 첫 번째 행 전체
     *     color = "FFFF00"  // 노란색
     * )
     * ```
     */
    fun highlightCells(vararg cells: Pair<Int, Int>, color: String) {
        cells.forEach { (row, col) ->
            table.getRow(row).getCell(col).color = color
        }
    }
    
    /**
     * 행 전체 하이라이트
     * 
     * @param rowIndex 행 인덱스
     * @param color 배경색 16진수 코드
     */
    fun highlightRow(rowIndex: Int, color: String) {
        val row = table.getRow(rowIndex)
        row.tableCells.forEach { cell ->
            cell.color = color
        }
    }
    
    /**
     * 열 전체 하이라이트
     * 
     * @param colIndex 열 인덱스
     * @param color 배경색 16진수 코드
     */
    fun highlightColumn(colIndex: Int, color: String) {
        for (rowIndex in 0 until table.numberOfRows) {
            table.getRow(rowIndex).getCell(colIndex).color = color
        }
    }

    /**
     * 테이블 테두리 설정
     * 
     * @param top 상단 테두리 스타일
     * @param bottom 하단 테두리 스타일
     * @param insideH 내부 수평 테두리 스타일
     * @param left 좌측 테두리 스타일
     * @param right 우측 테두리 스타일
     * @param insideV 내부 수직 테두리 스타일
     * @param excludedColumns 테두리 제외할 열 집합
     */
    fun setBordersWithCells(
        top: TableBorderStyle? = null,
        bottom: TableBorderStyle? = null,
        insideH: TableBorderStyle? = null,
        left: TableBorderStyle? = null,
        right: TableBorderStyle? = null,
        insideV: TableBorderStyle? = null,
        excludedColumns: Set<Int> = emptySet()
    ) {
        val tblPr = table.ctTbl.tblPr ?: table.ctTbl.addNewTblPr()
        val tblBorders = tblPr.addNewTblBorders()

        top?.let { style ->
            tblBorders.addNewTop().apply {
                `val` = style.xmlValue
                sz = BigInteger.valueOf(style.size.toLong())
                color = style.color
            }
        }

        bottom?.let { style ->
            tblBorders.addNewBottom().apply {
                `val` = style.xmlValue
                sz = BigInteger.valueOf(style.size.toLong())
                color = style.color
            }
        }

        insideH?.let { style ->
            tblBorders.addNewInsideH().apply {
                `val` = style.xmlValue
                sz = BigInteger.valueOf(style.size.toLong())
                color = style.color
            }
        }

        left?.let { style ->
            tblBorders.addNewLeft().apply {
                `val` = style.xmlValue
                sz = BigInteger.valueOf(style.size.toLong())
                color = style.color
            }
        }

        right?.let { style ->
            tblBorders.addNewRight().apply {
                `val` = style.xmlValue
                sz = BigInteger.valueOf(style.size.toLong())
                color = style.color
            }
        }

        insideV?.let { style ->
            tblBorders.addNewInsideV().apply {
                `val` = style.xmlValue
                sz = BigInteger.valueOf(style.size.toLong())
                color = style.color
            }
        }
    }
    
    /**
     * 간단한 테두리 설정 (모든 테두리 동일)
     * 
     * @param style 테두리 스타일
     */
    fun setBorders(style: TableBorderStyle) {
        setBordersWithCells(
            top = style,
            bottom = style,
            insideH = style,
            left = style,
            right = style,
            insideV = style
        )
    }
    
    /**
     * 테두리 제거
     */
    fun removeBorders() {
        table.removeBorders()
    }
    
    /**
     * 테이블 정렬 설정
     */
    fun alignment(alignment: STJcTable.Enum) {
        table.ctTbl.tblPr.addNewJc().`val` = alignment
    }
    
    /**
     * 헤더 행 설정 (첫 번째 행을 헤더로)
     * 
     * @param backgroundColor 헤더 배경색 (기본: "E0E0E0")
     * @param textBold 헤더 텍스트 굵게 여부 (기본: true)
     */
    fun setupHeaderRow(backgroundColor: String = "E0E0E0", textBold: Boolean = true) {
        if (table.numberOfRows > 0) {
            highlightRow(0, backgroundColor)
            
            val headerRow = table.getRow(0)
            headerRow.tableCells.forEachIndexed { colIndex, cell ->
                // 기존 텍스트를 굵게 만들기 (이미 텍스트가 있는 경우)
                if (textBold && cell.paragraphs.isNotEmpty()) {
                    cell.paragraphs.forEach { paragraph ->
                        paragraph.runs.forEach { run ->
                            run.isBold = true
                        }
                    }
                }
            }
        }
    }
}

/**
 * 테이블 테두리 스타일 정의
 */
enum class TableBorderStyle(
    val xmlValue: STBorder.Enum,
    val size: Int = 4,
    val color: String = "000000"
) {
    SINGLE(STBorder.SINGLE),
    DOUBLE(STBorder.DOUBLE, 6),
    THICK(STBorder.THICK, 8),
    DOTTED(STBorder.DOTTED, 2),
    DASHED(STBorder.DASHED, 4),
    NONE(STBorder.NONE, 0);
    
    companion object {
        /**
         * 커스텀 테두리 스타일 생성
         */
        fun custom(
            style: STBorder.Enum = STBorder.SINGLE,
            size: Int = 4,
            color: String = "000000"
        ): TableBorderStyle {
            return when (style) {
                STBorder.SINGLE -> SINGLE
                STBorder.DOUBLE -> DOUBLE
                STBorder.THICK -> THICK
                STBorder.DOTTED -> DOTTED
                STBorder.DASHED -> DASHED
                STBorder.NONE -> NONE
                else -> SINGLE
            }
        }
    }
}