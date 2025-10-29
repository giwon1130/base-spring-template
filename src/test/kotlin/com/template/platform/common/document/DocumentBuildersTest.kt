package com.template.platform.common.document

import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

class DocumentBuildersTest {

    @Test
    fun `Word 문서 생성 기본 테스트`() {
        // Given & When: 기본 문서 생성
        val doc = document {
            margins {
                allInches(1.0)  // 1인치 마진
            }
            
            heading("테스트 문서", level = 1, alignment = ParagraphAlignment.CENTER)
            
            paragraph {
                text("이것은 테스트 문단입니다.", fontSize = 12, fontFamily = "Arial")
            }
            
            emptyLine(count = 1)
            
            table(rows = 2, cols = 3) {
                // 헤더 행
                cell(0, 0) { text("컬럼1", isBold = true) }
                cell(0, 1) { text("컬럼2", isBold = true) }
                cell(0, 2) { text("컬럼3", isBold = true) }
                
                // 데이터 행
                cell(1, 0) { text("데이터1") }
                cell(1, 1) { text("데이터2") }
                cell(1, 2) { text("데이터3") }
                
                setupHeaderRow()
            }
        }

        // Then: 문서가 성공적으로 생성되어야 함
        assertNotNull(doc)
        assertTrue(doc.paragraphs.isNotEmpty())
        assertTrue(doc.tables.isNotEmpty())
        
        // 문서를 바이트 배열로 변환 가능한지 확인
        val outputStream = ByteArrayOutputStream()
        doc.write(outputStream)
        assertTrue(outputStream.size() > 0)
        
        doc.close()
    }

    @Test
    fun `텍스트 블록 믹스 테스트`() {
        // Given: 다양한 스타일의 텍스트 블록들
        val textBlocks = arrayOf(
            TextBlock("굵은 텍스트 ", isBold = true),
            TextBlock("일반 텍스트 ", isBold = false),
            TextBlock("빨간 텍스트", color = "FF0000", isBold = true)
        )

        // When: 문서 생성
        val doc = document {
            paragraph {
                mixedText(*textBlocks)
            }
        }

        // Then: 문서가 생성되고 문단이 있어야 함
        assertNotNull(doc)
        assertEquals(1, doc.paragraphs.size)
        assertEquals(3, doc.paragraphs[0].runs.size)
        
        doc.close()
    }

    @Test
    fun `테이블 스타일링 테스트`() {
        // Given & When: 스타일링된 테이블 생성
        val doc = document {
            table(rows = 3, cols = 2, columnWidths = listOf(0.7, 0.3)) {
                // 셀 내용 설정
                cell(0, 0) { text("제목", isBold = true, fontSize = 14) }
                cell(0, 1) { text("값", isBold = true, fontSize = 14) }
                
                cell(1, 0) { text("항목 1") }
                cell(1, 1) { text("100") }
                
                cell(2, 0) { text("항목 2") }
                cell(2, 1) { text("200") }
                
                // 헤더 행 스타일링
                setupHeaderRow(backgroundColor = "E0E0E0")
                
                // 특정 셀 하이라이트
                highlightCells(Pair(1, 1), color = "FFFF00")
                
                // 테두리 설정
                setBorders(TableBorderStyle.SINGLE)
            }
        }

        // Then: 테이블이 제대로 생성되어야 함
        assertNotNull(doc)
        assertEquals(1, doc.tables.size)
        assertEquals(3, doc.tables[0].numberOfRows)
        assertEquals(2, doc.tables[0].getRow(0).tableCells.size)
        
        doc.close()
    }

    @Test
    fun `이미지 삽입 테스트`() {
        // Given: 테스트용 이미지 생성
        val testImage = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).apply {
            val g = createGraphics()
            g.color = Color.BLUE
            g.fillRect(0, 0, 100, 100)
            g.dispose()
        }

        // When: 이미지가 포함된 문서 생성
        val doc = document {
            paragraph {
                text("이미지 테스트")
            }
            
            paragraph(alignment = ParagraphAlignment.CENTER) {
                image(testImage, 200.0, 200.0)
            }
            
            table(rows = 1, cols = 1) {
                cell(0, 0) {
                    image(testImage, 150.0, 150.0)
                }
            }
        }

        // Then: 문서가 성공적으로 생성되어야 함
        assertNotNull(doc)
        assertTrue(doc.paragraphs.size >= 2)
        assertTrue(doc.tables.isNotEmpty())
        
        doc.close()
    }

    @Test
    fun `문서 레이아웃 테스트`() {
        // Given & When: 복잡한 레이아웃의 문서 생성
        val doc = document {
            margins {
                top = 1440    // 1인치
                bottom = 1440
                left = 1800   // 1.25인치
                right = 1800
            }
            
            // 제목 페이지
            heading("보고서 제목", level = 1, alignment = ParagraphAlignment.CENTER)
            emptyLine(count = 2)
            
            paragraph(alignment = ParagraphAlignment.CENTER) {
                text("부제목", fontSize = 14, color = "666666")
            }
            
            pageBreak()
            
            // 내용 페이지
            heading("1. 개요", level = 2)
            
            paragraph {
                text("이 보고서는 테스트 목적으로 생성되었습니다. ", fontSize = 11)
                mixedText(
                    TextBlock("중요한 정보", isBold = true, color = "FF0000"),
                    TextBlock("는 빨간색으로 표시됩니다.", fontSize = 11)
                )
            }
            
            heading("2. 데이터", level = 2)
            
            table(rows = 4, cols = 3) {
                // 데이터 입력
                repeat(4) { row ->
                    repeat(3) { col ->
                        cell(row, col) {
                            text(
                                if (row == 0) "헤더 ${col + 1}" else "데이터 $row-$col",
                                isBold = row == 0
                            )
                        }
                    }
                }
                
                setupHeaderRow()
                setBorders(TableBorderStyle.SINGLE)
            }
            
            horizontalLine()
            
            paragraph(alignment = ParagraphAlignment.RIGHT) {
                text("보고서 끝", fontSize = 10, color = "999999")
            }
        }

        // Then: 복잡한 문서가 성공적으로 생성되어야 함
        assertNotNull(doc)
        assertTrue(doc.paragraphs.size > 5)
        assertTrue(doc.tables.isNotEmpty())
        
        // 바이트 배열 변환 확인
        val outputStream = ByteArrayOutputStream()
        doc.write(outputStream)
        assertTrue(outputStream.size() > 1000) // 충분한 크기의 문서
        
        doc.close()
    }

    @Test
    fun `Margins 클래스 테스트`() {
        // Given: Margins 객체 생성
        val margins = Margins().apply {
            allInches(1.5)
        }

        // Then: 마진이 올바르게 설정되어야 함
        assertEquals(2160, margins.top)    // 1.5인치 * 1440 = 2160 twips
        assertEquals(2160, margins.bottom)
        assertEquals(2160, margins.left)
        assertEquals(2160, margins.right)
        
        // When: 센티미터로 설정
        margins.allCentimeters(2.5)
        
        // Then: 센티미터 변환이 올바르게 되어야 함
        val expectedTwips = (2.5 * 567).toInt() // 2.5cm * 567 = 1417.5 ≈ 1417 twips
        assertEquals(expectedTwips, margins.top)
        assertEquals(expectedTwips, margins.bottom)
        assertEquals(expectedTwips, margins.left)
        assertEquals(expectedTwips, margins.right)
    }
}