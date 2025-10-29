package com.template.platform.common.image

import java.awt.*

/**
 * 이미지 렌더링 스타일 설정 클래스
 * 
 * 주요 기능:
 * - 폴리곤/라벨 색상 설정
 * - 폰트 및 텍스트 스타일 설정
 * - 선 두께 및 투명도 설정
 * 
 * BMOA 프로젝트에서 이전됨
 */
data class RenderStyle(
    /** 추가된 객체 색상 */
    val addedColor: Color = Color.GREEN,
    
    /** 삭제된 객체 색상 */
    val deletedColor: Color = Color.RED,
    
    /** 기본/변화없음 객체 색상 */
    val defaultColor: Color = Color.GRAY,
    
    /** 유지된 객체 색상 */
    val retainedColor: Color = Color.BLUE,
    
    /** 고위험도 색상 */
    val highColor: Color = Color.RED,
    
    /** 중위험도 색상 */
    val mediumColor: Color = Color.YELLOW,
    
    /** 저위험도 색상 */
    val lowColor: Color = Color.GREEN,
    
    /** 텍스트 폰트 */
    val font: Font = Font("Arial", Font.BOLD, 12),
    
    /** 선 스타일 (두께, 대시 등) */
    val stroke: Stroke = BasicStroke(2f),
    
    /** 라벨 텍스트 오프셋 (픽셀) */
    val labelOffset: Int = 15,
    
    /** 폴리곤 내부 투명도 (0-255) */
    val fillAlpha: Int = 180,
    
    /** 폴리곤 테두리 투명도 (0-255) */
    val borderAlpha: Int = 220
) {
    
    companion object {
        /**
         * 변화탐지용 기본 스타일
         */
        fun changeDetectionStyle(): RenderStyle = RenderStyle(
            addedColor = Color(0, 255, 0),      // 밝은 녹색
            deletedColor = Color(255, 0, 0),    // 밝은 빨간색
            retainedColor = Color(0, 0, 255),   // 파란색
            defaultColor = Color(128, 128, 128), // 회색
            font = Font("Arial", Font.BOLD, 14),
            stroke = BasicStroke(2f),
            labelOffset = 20
        )
        
        /**
         * 위험도 분석용 스타일
         */
        fun riskAnalysisStyle(): RenderStyle = RenderStyle(
            highColor = Color(255, 0, 0),       // 빨간색
            mediumColor = Color(255, 165, 0),   // 주황색
            lowColor = Color(0, 255, 0),        // 녹색
            defaultColor = Color(128, 128, 128), // 회색
            font = Font("Arial", Font.BOLD, 16),
            stroke = BasicStroke(3f),
            labelOffset = 25
        )
        
        /**
         * 미니맵용 간단한 스타일
         */
        fun minimapStyle(): RenderStyle = RenderStyle(
            addedColor = Color(0, 200, 0),
            deletedColor = Color(200, 0, 0),
            defaultColor = Color(100, 100, 100),
            font = Font("Arial", Font.PLAIN, 10),
            stroke = BasicStroke(1f),
            labelOffset = 10,
            fillAlpha = 120,
            borderAlpha = 180
        )
    }
    
    /**
     * 상태에 따른 색상 선택
     */
    fun getColorByStatus(status: String?): Color {
        return when (status?.lowercase()) {
            "added" -> addedColor
            "deleted" -> deletedColor
            "retained" -> retainedColor
            "high" -> highColor
            "medium" -> mediumColor
            "low" -> lowColor
            else -> defaultColor
        }
    }
    
    /**
     * 투명도가 적용된 색상 반환
     */
    fun getColorWithAlpha(baseColor: Color, alpha: Int): Color {
        return Color(baseColor.red, baseColor.green, baseColor.blue, alpha.coerceIn(0, 255))
    }
}