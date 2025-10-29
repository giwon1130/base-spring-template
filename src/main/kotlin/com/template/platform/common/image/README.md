# Image Processing Utilities

BMOA 프로젝트에서 이전된 재사용 가능한 이미지 처리 및 지도 렌더링 유틸리티 모음입니다.

## 📦 모듈 구조

```
common/image/
├── ImageUtils.kt              # 기본 이미지 처리 유틸리티
├── MapTileUtils.kt           # 지도 좌표 변환 함수들
├── RenderStyle.kt            # 렌더링 스타일 설정
├── GraphicsDrawUtils.kt      # 폴리곤/라벨 그리기
└── tile/                     # 지도 타일 관련
    ├── TileProvider.kt       # 타일 제공자 인터페이스
    ├── ArcGisTileProvider.kt # ArcGIS 위성영상 타일
    ├── OpenStreetMapTileProvider.kt # OSM 지도 타일
    └── TileProviderFactory.kt # 타일 제공자 팩토리
```

## 🚀 주요 기능

### 1. 이미지 처리 (`ImageUtils`)
- 이미지 포맷 변환 (BufferedImage ↔ ByteArray ↔ InputStream)
- 이미지 크기 조정 및 리사이징
- 지원 포맷 확인

### 2. 지도 좌표 변환 (`MapTileUtils`)
- 위경도 ↔ 타일 좌표 변환
- 위경도 ↔ 픽셀 좌표 변환
- 거리 계산 (Haversine 공식)
- 미터/픽셀 변환

### 3. 그래픽 렌더링 (`GraphicsDrawUtils`, `RenderStyle`)
- 폴리곤 및 라벨 그리기
- 다양한 렌더링 스타일 지원
- 투명도 처리 (구멍 뚫기 효과)
- 텍스트 오버레이

### 4. 지도 타일 서비스 (`tile/`)
- 다양한 타일 제공자 지원 (ArcGIS, OSM)
- 타일 캐싱 및 폴백 지원
- 배치 타일 로딩

## 💡 사용 예제

### 기본 이미지 처리
```kotlin
import com.template.platform.common.image.ImageUtils
import java.awt.image.BufferedImage

// 이미지 리사이징
val originalImage: BufferedImage = // ... 원본 이미지
val resizedImage = ImageUtils.resizeImage(originalImage, 800, 600)

// 이미지 포맷 변환
val byteArray = ImageUtils.bufferedImageToByteArray(resizedImage, "PNG")
val inputStream = ImageUtils.bufferedImageToInputStream(resizedImage, "JPEG")
```

### 지도 좌표 변환
```kotlin
import com.template.platform.common.image.MapTileUtils

// 위경도를 타일 좌표로 변환
val (tileX, tileY) = MapTileUtils.lonLatToTileXY(126.9784, 37.5665, 15) // 서울시청

// 위경도를 픽셀 좌표로 변환
val pixelX = MapTileUtils.lonToPixelX(126.9784, 15)
val pixelY = MapTileUtils.latToPixelY(37.5665, 15)

// 두 지점 간 거리 계산
val distance = MapTileUtils.calculateDistance(
    126.9784, 37.5665,  // 서울시청
    129.0756, 35.1796   // 부산시청
) // 결과: 약 325km
```

### 폴리곤 렌더링
```kotlin
import com.template.platform.common.image.*
import java.awt.Graphics2D
import java.awt.image.BufferedImage

// 렌더링 스타일 설정
val style = RenderStyle.changeDetectionStyle()

// 폴리곤 데이터 준비
val polygon = listOf(
    126.9784 to 37.5665,  // 서울시청
    126.9794 to 37.5665,  // 동쪽으로 이동
    126.9794 to 37.5675,  // 북쪽으로 이동
    126.9784 to 37.5675   // 서쪽으로 이동
)

val request = GraphicsDrawUtils.LabelDrawRequest(
    polygon = polygon,
    center = 126.9789 to 37.5670,
    labelClass = "건물",
    status = "added"
)

// 좌표 변환 함수
val coordinateMapper: (Double, Double) -> Pair<Int, Int> = { lon, lat ->
    MapTileUtils.lonToPixelX(lon, 15) to MapTileUtils.latToPixelY(lat, 15)
}

// 이미지에 폴리곤 그리기
val image = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB)
val g2d = image.createGraphics()

GraphicsDrawUtils.drawPolygonAndLabel(
    g = g2d,
    request = request,
    coordinateMapper = coordinateMapper,
    style = style,
    drawLabelText = true
)

g2d.dispose()
```

### 지도 타일 사용
```kotlin
import com.template.platform.common.image.tile.*

// 타일 제공자 사용
val arcgisProvider = ArcGisTileProvider()
val osmProvider = OpenStreetMapTileProvider()

// 특정 타일 가져오기
val tile = arcgisProvider.getTile(x = 55166, y = 25332, z = 16) // 서울 지역

// 팩토리를 통한 타일 관리
val osmTile = TileProviderFactory.getOsmTile(55166, 25332, 16)
val arcgisTile = TileProviderFactory.getArcGisTile(55166, 25332, 16)

// 폴백 지원
val tileWithFallback = TileProviderFactory.getTileWithFallback(
    primaryProvider = "arcgis",
    fallbackProviders = listOf("osm"),
    x = 55166, y = 25332, z = 16
)

// 캐시 상태 확인
val cacheStats = TileProviderFactory.getCacheStats()
println("캐시 크기: ${cacheStats["size"]}")
```

## 🔧 설정 및 최적화

### 타일 캐시 설정
```kotlin
// 캐시 최대 크기 설정 (기본: 1000개)
TileProviderFactory.setMaxCacheSize(2000)

// 캐시 정리
TileProviderFactory.clearCache()
```

### 커스텀 렌더링 스타일
```kotlin
val customStyle = RenderStyle(
    addedColor = Color.GREEN,
    deletedColor = Color.RED,
    font = Font("Arial", Font.BOLD, 16),
    stroke = BasicStroke(3f),
    labelOffset = 25,
    fillAlpha = 150,
    borderAlpha = 200
)
```

## 🌍 지원하는 지도 서비스

### ArcGIS World Imagery
- **장점**: 고해상도 위성영상, 전 세계 커버리지
- **단점**: 상업적 사용 시 라이선스 필요
- **줌 레벨**: 0-19

### OpenStreetMap
- **장점**: 무료 오픈소스, 전 세계 커버리지
- **단점**: User-Agent 설정 필수, rate limiting
- **줌 레벨**: 0-19
- **사용 정책**: https://operations.osmfoundation.org/policies/tiles/

## 📝 참고사항

1. **메모리 사용량**: 타일 캐시는 메모리를 많이 사용할 수 있으므로 적절한 크기 제한 필요
2. **네트워크 정책**: 각 타일 서비스의 사용 정책 준수 필요
3. **좌표계**: 웹 메르카토르 투영법(EPSG:3857) 기반
4. **성능**: 대량의 타일을 처리할 때는 비동기 처리 고려

## 🔗 관련 문서

- [BMOA 원본 프로젝트](../../../../../../../BHC/BMOA/BMOA-api-server)
- [Platform Template 프로젝트 구조](../../../../../README.md)
- [Common 모듈 가이드](../../README.md)