package com.template.platform.common.image

import java.awt.*

/**
 * 그래픽 드로잉을 위한 공통 유틸리티 클래스
 * 
 * 주요 기능:
 * - 폴리곤 및 라벨 그리기
 * - 텍스트 오버레이
 * - 투명도 처리 (구멍 뚫기 효과)
 * - 다양한 렌더링 스타일 지원
 * 
 * BMOA 프로젝트에서 이전된 재사용 가능한 그래픽 함수들
 */
object GraphicsDrawUtils {
    
    /**
     * 라벨 드로우 요청 데이터 클래스
     */
    data class LabelDrawRequest(
        /** 폴리곤 좌표 리스트 (경도, 위도) */
        val polygon: List<Pair<Double, Double>>,
        /** 중심점 좌표 (경도, 위도) */
        val center: Pair<Double, Double>,
        /** 라벨 클래스/텍스트 */
        val labelClass: String?,
        /** 상태 ("added", "deleted", "retained", "high", "medium", "low" 등) */
        val status: String?
    )
    
    /**
     * 폴리곤과 라벨을 함께 그리는 함수
     * 
     * @param g Graphics2D 컨텍스트
     * @param request 라벨 드로우 요청 정보
     * @param coordinateMapper 위경도 → 픽셀 변환 함수
     * @param style 렌더링 스타일
     * @param drawLabelText 텍스트 렌더링 여부
     * @param useColoredBorder 테두리도 같은 색상으로 할지 여부
     */
    fun drawPolygonAndLabel(
        g: Graphics2D,
        request: LabelDrawRequest,
        coordinateMapper: (Double, Double) -> Pair<Int, Int>,
        style: RenderStyle,
        drawLabelText: Boolean = true,
        useColoredBorder: Boolean = false
    ) {
        val xPoints = request.polygon.map { (lon, lat) -> coordinateMapper(lon, lat).first }.toIntArray()
        val yPoints = request.polygon.map { (lon, lat) -> coordinateMapper(lon, lat).second }.toIntArray()

        // 상태에 따른 폴리곤 색상 선택
        val fillColor = style.getColorByStatus(request.status)
        
        // 1. 폴리곤 내부 채우기
        g.color = style.getColorWithAlpha(fillColor, style.fillAlpha)
        g.fillPolygon(xPoints, yPoints, xPoints.size)

        // 2. 폴리곤 테두리 그리기
        if (useColoredBorder) {
            g.color = style.getColorWithAlpha(fillColor, style.borderAlpha)
        } else {
            g.color = style.defaultColor
        }
        g.stroke = style.stroke
        g.drawPolygon(xPoints, yPoints, xPoints.size)

        // 3. 텍스트 그리기
        if (drawLabelText) {
            drawLabel(g, request, coordinateMapper, style, fillColor)
        }
    }
    
    /**
     * 구멍 뚫기 효과를 포함한 폴리곤 그리기
     * 
     * @param g Graphics2D 컨텍스트
     * @param request 라벨 드로우 요청 정보
     * @param coordinateMapper 위경도 → 픽셀 변환 함수
     * @param style 렌더링 스타일
     * @param drawLabelText 텍스트 렌더링 여부
     * @param useColoredBorder 테두리도 같은 색상으로 할지 여부
     * @param asHole 폴리곤을 투명으로 뚫을지 여부
     * @param holeBorder 구멍 가장자리 선 색상
     * @param holeBorderStroke 구멍 가장자리 선 두께
     */
    fun drawPolygonWithHole(
        g: Graphics2D,
        request: LabelDrawRequest,
        coordinateMapper: (Double, Double) -> Pair<Int, Int>,
        style: RenderStyle,
        drawLabelText: Boolean = true,
        useColoredBorder: Boolean = false,
        asHole: Boolean = false,
        holeBorder: Color? = null,
        holeBorderStroke: Stroke = BasicStroke(1f)
    ) {
        val xPoints = request.polygon.map { (lon, lat) -> coordinateMapper(lon, lat).first }.toIntArray()
        val yPoints = request.polygon.map { (lon, lat) -> coordinateMapper(lon, lat).second }.toIntArray()

        val fillColor = style.getColorByStatus(request.status)

        // 구멍 모드인 경우
        if (asHole) {
            val previousComposite = g.composite
            // 영역을 완전 투명으로 만들기
            g.composite = AlphaComposite.Clear
            g.fillPolygon(xPoints, yPoints, xPoints.size)
            // 컴포지트 복구
            g.composite = previousComposite

            // 구멍 외곽선 그리기 (선택적)
            if (holeBorder != null) {
                g.color = holeBorder
                g.stroke = holeBorderStroke
                g.drawPolygon(xPoints, yPoints, xPoints.size)
            }
            return // 구멍일 때는 텍스트도 그리지 않음
        }

        // 일반 모드
        drawPolygonAndLabel(g, request, coordinateMapper, style, drawLabelText, useColoredBorder)
    }
    
    /**
     * 텍스트(라벨)만 그리는 함수
     * 
     * @param g Graphics2D 컨텍스트
     * @param request 라벨 드로우 요청 정보
     * @param coordinateMapper 위경도 → 픽셀 변환 함수
     * @param style 렌더링 스타일
     */
    fun drawLabelOnly(
        g: Graphics2D,
        request: LabelDrawRequest,
        coordinateMapper: (Double, Double) -> Pair<Int, Int>,
        style: RenderStyle
    ) {
        val fillColor = style.getColorByStatus(request.status)
        drawLabel(g, request, coordinateMapper, style, fillColor)
    }
    
    /**
     * 라벨 텍스트를 그리는 내부 함수
     */
    private fun drawLabel(
        g: Graphics2D,
        request: LabelDrawRequest,
        coordinateMapper: (Double, Double) -> Pair<Int, Int>,
        style: RenderStyle,
        backgroundColor: Color
    ) {
        val labelText = request.labelClass?.takeIf { it.isNotBlank() } ?: "?"
        val (cx, cy) = coordinateMapper(request.center.first, request.center.second)

        val g2 = g.create() as Graphics2D
        try {
            g2.font = style.font
            val metrics = g2.fontMetrics
            val textWidth = metrics.stringWidth(labelText)
            val textHeight = metrics.height

            val rectX = cx - textWidth / 2 - 2
            val rectY = (cy - style.labelOffset - metrics.ascent).coerceAtLeast(0)
            val rectW = textWidth + 4
            val rectH = textHeight

            // 텍스트 배경 사각형 그리기
            g2.color = backgroundColor
            g2.fillRoundRect(rectX, rectY, rectW, rectH, 10, 10)

            // 텍스트 그리기
            g2.color = Color.WHITE
            g2.drawString(labelText, cx - textWidth / 2, cy - style.labelOffset)
        } finally {
            g2.dispose()
        }
    }
    
    /**
     * 단순한 폴리곤 그리기 (라벨 없음)
     * 
     * @param g Graphics2D 컨텍스트
     * @param polygon 폴리곤 좌표 리스트
     * @param coordinateMapper 위경도 → 픽셀 변환 함수
     * @param fillColor 채우기 색상
     * @param borderColor 테두리 색상
     * @param stroke 선 스타일
     * @param fillAlpha 채우기 투명도 (0-255)
     */
    fun drawSimplePolygon(
        g: Graphics2D,
        polygon: List<Pair<Double, Double>>,
        coordinateMapper: (Double, Double) -> Pair<Int, Int>,
        fillColor: Color,
        borderColor: Color = fillColor,
        stroke: Stroke = BasicStroke(1f),
        fillAlpha: Int = 180
    ) {
        val xPoints = polygon.map { (lon, lat) -> coordinateMapper(lon, lat).first }.toIntArray()
        val yPoints = polygon.map { (lon, lat) -> coordinateMapper(lon, lat).second }.toIntArray()

        // 내부 채우기
        g.color = Color(fillColor.red, fillColor.green, fillColor.blue, fillAlpha)
        g.fillPolygon(xPoints, yPoints, xPoints.size)

        // 테두리 그리기
        g.color = borderColor
        g.stroke = stroke
        g.drawPolygon(xPoints, yPoints, xPoints.size)
    }
    
    /**
     * 텍스트를 배경과 함께 그리는 함수
     * 
     * @param g Graphics2D 컨텍스트
     * @param text 텍스트
     * @param x X 좌표
     * @param y Y 좌표
     * @param font 폰트
     * @param textColor 텍스트 색상
     * @param backgroundColor 배경 색상
     * @param padding 패딩 (픽셀)
     */
    fun drawTextWithBackground(
        g: Graphics2D,
        text: String,
        x: Int,
        y: Int,
        font: Font = Font("Arial", Font.BOLD, 12),
        textColor: Color = Color.WHITE,
        backgroundColor: Color = Color.BLACK,
        padding: Int = 4
    ) {
        val g2 = g.create() as Graphics2D
        try {
            g2.font = font
            val metrics = g2.fontMetrics
            val textWidth = metrics.stringWidth(text)
            val textHeight = metrics.height

            val rectX = x - textWidth / 2 - padding
            val rectY = y - metrics.ascent - padding
            val rectW = textWidth + 2 * padding
            val rectH = textHeight + padding

            // 배경 사각형
            g2.color = backgroundColor
            g2.fillRoundRect(rectX, rectY, rectW, rectH, 8, 8)

            // 텍스트
            g2.color = textColor
            g2.drawString(text, x - textWidth / 2, y)
        } finally {
            g2.dispose()
        }
    }
}