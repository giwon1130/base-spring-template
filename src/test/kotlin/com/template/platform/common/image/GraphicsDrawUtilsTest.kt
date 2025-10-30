package com.template.platform.common.image

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.image.BufferedImage

class GraphicsDrawUtilsTest {

    @Test
    fun `폴리곤과 라벨 그리기 테스트`() {
        // Given
        val image = BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        val request = GraphicsDrawUtils.LabelDrawRequest(
            polygon = listOf(
                50.0 to 50.0,
                150.0 to 50.0,
                150.0 to 150.0,
                50.0 to 150.0
            ),
            center = 100.0 to 100.0,
            labelClass = "Test Label",
            status = "added"
        )
        
        val style = RenderStyle.changeDetectionStyle()
        val coordinateMapper: (Double, Double) -> Pair<Int, Int> = { x, y -> x.toInt() to y.toInt() }

        // When - 예외 없이 실행되는지 확인
        var drawingSuccessful = false
        try {
            GraphicsDrawUtils.drawPolygonAndLabel(g2d, request, coordinateMapper, style, true, false)
            drawingSuccessful = true
        } finally {
            g2d.dispose()
        }

        // Then - 그래픽 함수가 정상 실행되었는지 확인
        assertThat(drawingSuccessful).isTrue
        assertThat(image).isNotNull
        assertThat(image.width).isEqualTo(400)
        assertThat(image.height).isEqualTo(400)
    }

    @Test
    fun `삭제된 객체 상태로 폴리곤 그리기 테스트`() {
        // Given
        val image = BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        val request = GraphicsDrawUtils.LabelDrawRequest(
            polygon = listOf(
                50.0 to 50.0,
                150.0 to 50.0,
                150.0 to 150.0,
                50.0 to 150.0
            ),
            center = 100.0 to 100.0,
            labelClass = "Deleted Object",
            status = "deleted"
        )
        
        val style = RenderStyle.changeDetectionStyle()
        val coordinateMapper: (Double, Double) -> Pair<Int, Int> = { x, y -> x.toInt() to y.toInt() }

        // When
        GraphicsDrawUtils.drawPolygonAndLabel(g2d, request, coordinateMapper, style, true, true)
        g2d.dispose()

        // Then
        val centerPixel = Color(image.getRGB(100, 100))
        // 삭제된 객체는 빨간색 계열이어야 함
        assertThat(centerPixel.red).isGreaterThan(centerPixel.green)
        assertThat(centerPixel.red).isGreaterThan(centerPixel.blue)
    }

    @Test
    fun `구멍 뚫기 효과 포함 폴리곤 그리기 테스트`() {
        // Given
        val image = BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        // 배경을 흰색으로 채우기
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, 300, 300)
        
        val request = GraphicsDrawUtils.LabelDrawRequest(
            polygon = listOf(
                50.0 to 50.0,
                150.0 to 50.0,
                150.0 to 150.0,
                50.0 to 150.0
            ),
            center = 100.0 to 100.0,
            labelClass = "Hole Effect",
            status = "retained"
        )
        
        val style = RenderStyle.riskAnalysisStyle()
        val coordinateMapper: (Double, Double) -> Pair<Int, Int> = { x, y -> x.toInt() to y.toInt() }

        // When
        GraphicsDrawUtils.drawPolygonWithHole(
            g2d, request, coordinateMapper, style, 
            drawLabelText = true, useColoredBorder = true, asHole = true
        )
        g2d.dispose()

        // Then
        assertThat(image).isNotNull
    }

    @Test
    fun `RenderStyle 다양한 스타일 테스트`() {
        // When
        val changeStyle = RenderStyle.changeDetectionStyle()
        val riskStyle = RenderStyle.riskAnalysisStyle()
        val minimapStyle = RenderStyle.minimapStyle()

        // Then
        assertThat(changeStyle.addedColor).isEqualTo(Color(0, 255, 0))
        assertThat(changeStyle.deletedColor).isEqualTo(Color(255, 0, 0))
        
        assertThat(riskStyle.highColor).isEqualTo(Color(255, 0, 0))
        assertThat(riskStyle.mediumColor).isEqualTo(Color(255, 165, 0))
        assertThat(riskStyle.lowColor).isEqualTo(Color(0, 255, 0))
        
        assertThat(minimapStyle.fillAlpha).isEqualTo(120)
        assertThat(minimapStyle.borderAlpha).isEqualTo(180)
    }

    @Test
    fun `상태별 색상 선택 테스트`() {
        // Given
        val style = RenderStyle.changeDetectionStyle()

        // When & Then
        assertThat(style.getColorByStatus("added")).isEqualTo(style.addedColor)
        assertThat(style.getColorByStatus("deleted")).isEqualTo(style.deletedColor)
        assertThat(style.getColorByStatus("retained")).isEqualTo(style.retainedColor)
        assertThat(style.getColorByStatus("unknown")).isEqualTo(style.defaultColor)
        assertThat(style.getColorByStatus(null)).isEqualTo(style.defaultColor)
    }

    @Test
    fun `투명도 적용된 색상 반환 테스트`() {
        // Given
        val style = RenderStyle()
        val baseColor = Color.RED

        // When
        val transparentColor = style.getColorWithAlpha(baseColor, 128)

        // Then
        assertThat(transparentColor.red).isEqualTo(255)
        assertThat(transparentColor.green).isEqualTo(0)
        assertThat(transparentColor.blue).isEqualTo(0)
        assertThat(transparentColor.alpha).isEqualTo(128)
    }

    @Test
    fun `투명도 범위 제한 테스트`() {
        // Given
        val style = RenderStyle()
        val baseColor = Color.BLUE

        // When & Then
        val lowAlpha = style.getColorWithAlpha(baseColor, -10)
        assertThat(lowAlpha.alpha).isEqualTo(0)

        val highAlpha = style.getColorWithAlpha(baseColor, 300)
        assertThat(highAlpha.alpha).isEqualTo(255)
    }

    @Test
    fun `복잡한 폴리곤 그리기 테스트`() {
        // Given
        val image = BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        // L자 모양 폴리곤
        val lShapeRequest = GraphicsDrawUtils.LabelDrawRequest(
            polygon = listOf(
                50.0 to 50.0,
                150.0 to 50.0,
                150.0 to 100.0,
                100.0 to 100.0,
                100.0 to 200.0,
                50.0 to 200.0
            ),
            center = 100.0 to 125.0,
            labelClass = "L-Shape",
            status = "high"
        )
        
        val style = RenderStyle.riskAnalysisStyle()
        val coordinateMapper: (Double, Double) -> Pair<Int, Int> = { x, y -> x.toInt() to y.toInt() }

        // When - 복잡한 폴리곤 그리기가 예외 없이 실행되는지 확인
        var drawingSuccessful = false
        try {
            GraphicsDrawUtils.drawPolygonAndLabel(g2d, lShapeRequest, coordinateMapper, style)
            drawingSuccessful = true
        } finally {
            g2d.dispose()
        }

        // Then - 복잡한 폴리곤 그리기 함수가 정상 실행되었는지 확인
        assertThat(drawingSuccessful).isTrue
        assertThat(image).isNotNull
        assertThat(lShapeRequest.polygon).hasSize(6) // L자 모양은 6개 점
        assertThat(lShapeRequest.labelClass).isEqualTo("L-Shape")
        assertThat(lShapeRequest.status).isEqualTo("high")
    }

    @Test
    fun `기본 RenderStyle 값 테스트`() {
        // When
        val defaultStyle = RenderStyle()

        // Then
        assertThat(defaultStyle.addedColor).isEqualTo(Color.GREEN)
        assertThat(defaultStyle.deletedColor).isEqualTo(Color.RED)
        assertThat(defaultStyle.defaultColor).isEqualTo(Color.GRAY)
        assertThat(defaultStyle.fillAlpha).isEqualTo(180)
        assertThat(defaultStyle.borderAlpha).isEqualTo(220)
        assertThat(defaultStyle.labelOffset).isEqualTo(15)
    }
}