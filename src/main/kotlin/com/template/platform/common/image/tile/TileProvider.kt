package com.template.platform.common.image.tile

import java.awt.image.BufferedImage

/**
 * 지도 타일 제공자 인터페이스
 * 
 * 다양한 지도 서비스(OpenStreetMap, ArcGIS, Google Maps 등)로부터
 * 타일 이미지를 가져오기 위한 공통 인터페이스
 * 
 * 표준 타일 좌표계 (z/x/y) 사용:
 * - z: 줌 레벨 (0~18)
 * - x: 타일 X 좌표 (0 ~ 2^z - 1)
 * - y: 타일 Y 좌표 (0 ~ 2^z - 1)
 * 
 * BMOA 프로젝트에서 이전됨
 */
interface TileProvider {
    
    /**
     * 지정된 좌표의 타일 이미지를 가져옴
     * 
     * @param x 타일 X 좌표
     * @param y 타일 Y 좌표  
     * @param z 줌 레벨
     * @return 타일 이미지 (실패 시 null)
     */
    fun getTile(x: Int, y: Int, z: Int): BufferedImage?
    
    /**
     * 타일 제공자 이름
     */
    val providerName: String
    
    /**
     * 타일 제공자 설명
     */
    val description: String
    
    /**
     * 지원하는 최대 줌 레벨
     */
    val maxZoomLevel: Int get() = 18
    
    /**
     * 지원하는 최소 줌 레벨
     */
    val minZoomLevel: Int get() = 0
    
    /**
     * 타일 크기 (픽셀)
     */
    val tileSize: Int get() = 256
    
    /**
     * 제공자 URL 패턴 (참고용)
     */
    val urlPattern: String
}