# Document Generation Utilities

BMOA 프로젝트에서 이전된 Word 문서 생성을 위한 재사용 가능한 유틸리티 모음입니다.

Apache POI 기반으로 Word 문서(.docx)를 프로그래밍 방식으로 생성할 수 있는 DSL(Domain Specific Language)을 제공합니다.

## 📦 모듈 구조

```
common/document/
├── DocsBuilder.kt          # 메인 문서 빌더 (DSL 엔트리포인트)
├── ParagraphBuilder.kt     # 문단 생성 및 스타일링
├── TableBuilder.kt         # 테이블 생성 및 관리
├── CellBuilder.kt          # 테이블 셀 스타일링
├── HeaderBuilder.kt        # 문서 헤더 관리
├── FootBuilder.kt          # 문서 푸터 관리
└── README.md              # 사용 가이드 (이 파일)
```

## 🚀 주요 기능

### 1. DSL 기반 문서 생성
- 직관적이고 읽기 쉬운 문서 생성 코드
- 중첩된 블록을 통한 구조화된 문서 작성
- 타입 안전성과 IDE 자동완성 지원

### 2. 풍부한 텍스트 스타일링
- 폰트, 크기, 색상, 굵기 설정
- 문단 정렬 및 간격 조정
- 혼합 스타일 텍스트 지원

### 3. 고급 테이블 기능
- 동적 열 너비 조정
- 셀별 개별 스타일링
- 테두리 및 배경색 설정
- 헤더 행 자동 스타일링

### 4. 이미지 처리
- BufferedImage 직접 삽입
- 자동 리사이징 및 크롭
- 셀 내 이미지 정렬

### 5. 헤더/푸터 관리
- 이미지 및 텍스트 조합
- 복잡한 레이아웃 지원
- 페이지 번호 자동 삽입

## 💡 사용 예제

### 기본 문서 생성

```kotlin
import com.template.platform.common.document.*
import org.apache.poi.xwpf.usermodel.ParagraphAlignment

val document = document {
    margins {
        allInches(1.0)  // 1인치 마진
    }
    
    heading("보고서 제목", level = 1, alignment = ParagraphAlignment.CENTER)
    
    paragraph {
        text("이것은 첫 번째 문단입니다.", fontSize = 12)
    }
    
    emptyLine()
    
    paragraph {
        mixedText(
            TextBlock("굵은 텍스트", isBold = true),
            TextBlock(" 일반 텍스트 "),
            TextBlock("빨간 텍스트", color = "FF0000")
        )
    }
}

// 파일로 저장
FileOutputStream("report.docx").use { out ->
    document.write(out)
}
document.close()
```

### 테이블 생성

```kotlin
val document = document {
    table(rows = 3, cols = 4, columnWidths = listOf(0.3, 0.2, 0.3, 0.2)) {
        // 헤더 행
        cell(0, 0) { text("이름", isBold = true) }
        cell(0, 1) { text("나이", isBold = true) }
        cell(0, 2) { text("부서", isBold = true) }
        cell(0, 3) { text("급여", isBold = true) }
        
        // 데이터 행
        cell(1, 0) { text("홍길동") }
        cell(1, 1) { text("30") }
        cell(1, 2) { text("개발팀") }
        cell(1, 3) { text("5000만원") }
        
        cell(2, 0) { text("김철수") }
        cell(2, 1) { text("25") }
        cell(2, 2) { text("디자인팀") }
        cell(2, 3) { text("4500만원") }
        
        // 스타일 적용
        setupHeaderRow(backgroundColor = "E0E0E0")
        highlightColumn(3, "FFFFCC")  // 급여 열 하이라이트
        setBorders(TableBorderStyle.SINGLE)
    }
}
```

### 이미지 삽입

```kotlin
import com.template.platform.common.image.ImageUtils
import java.awt.image.BufferedImage

val document = document {
    heading("이미지 보고서")
    
    // 문단에 이미지 삽입
    paragraph(alignment = ParagraphAlignment.CENTER) {
        imageAutoResize("/path/to/image.png", maxWidth = 400.0)
    }
    
    // 테이블 셀에 이미지 삽입
    table(rows = 2, cols = 2) {
        cell(0, 0) { text("설명") }
        cell(0, 1) { 
            imageCenteredWithBackground(
                "/path/to/chart.png",
                targetWidth = 300,
                targetHeight = 200
            )
        }
        
        cell(1, 0) { text("BufferedImage 사용") }
        cell(1, 1) {
            val testImage = BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB)
            // ... 이미지 그리기 코드 ...
            image(testImage, 200.0, 100.0)
        }
    }
}
```

### 헤더/푸터 설정

```kotlin
val document = document {
    // 헤더 설정
    header {
        imageWithText(
            logoInputStream,
            imageWidth = 100.0,
            imageHeight = 50.0,
            text = "회사명",
            layout = HeaderLayout.IMAGE_LEFT_TEXT_RIGHT,
            fontSize = 14,
            isBold = true
        )
        spacingAfter = 500
    }
    
    // 푸터 설정
    footer {
        threeColumnLayout(
            leftContent = "기밀 문서",
            centerContent = null,
            rightContent = "2024년 보고서"
        )
    }
    
    // 본문 내용
    heading("문서 제목")
    paragraph { text("문서 내용...") }
}
```

### 복합 문서 생성

```kotlin
val document = document {
    margins {
        allCentimeters(2.5)  // 2.5cm 마진
    }
    
    // 표지 페이지
    paragraph(alignment = ParagraphAlignment.CENTER) {
        text("연간 보고서", fontSize = 24, isBold = true)
    }
    emptyLine(count = 3)
    paragraph(alignment = ParagraphAlignment.CENTER) {
        text("2024년", fontSize = 18, color = "666666")
    }
    
    pageBreak()
    
    // 목차
    heading("목차", level = 1)
    paragraph { text("1. 개요 ........................... 3") }
    paragraph { text("2. 주요 성과 ..................... 4") }
    paragraph { text("3. 재무 현황 ..................... 5") }
    
    pageBreak()
    
    // 본문
    heading("1. 개요", level = 1)
    paragraph {
        text("올해는 회사에게 중요한 한 해였습니다. ")
        mixedText(
            TextBlock("매출이 전년 대비 ", fontSize = 12),
            TextBlock("15% 증가", isBold = true, color = "0066CC"),
            TextBlock("하였으며, 새로운 시장 진출에도 성공했습니다.", fontSize = 12)
        )
    }
    
    emptyLine()
    
    // 성과 테이블
    table(rows = 4, cols = 3) {
        cell(0, 0) { text("분기", isBold = true) }
        cell(0, 1) { text("매출", isBold = true) }
        cell(0, 2) { text("증가율", isBold = true) }
        
        cell(1, 0) { text("1분기") }
        cell(1, 1) { text("100억원") }
        cell(1, 2) { text("5%") }
        
        cell(2, 0) { text("2분기") }
        cell(2, 1) { text("120억원") }
        cell(2, 2) { text("20%") }
        
        cell(3, 0) { text("3분기") }
        cell(3, 1) { text("110억원") }
        cell(3, 2) { text("10%") }
        
        setupHeaderRow()
        setBorders(TableBorderStyle.SINGLE)
    }
    
    horizontalLine()
    
    paragraph(alignment = ParagraphAlignment.RIGHT) {
        text("보고서 생성일: ${java.time.LocalDate.now()}", 
             fontSize = 10, color = "999999")
    }
}
```

## 🎨 스타일링 옵션

### 텍스트 스타일
```kotlin
text(
    content = "텍스트 내용",
    fontSize = 12,           // 폰트 크기
    fontFamily = "Pretendard", // 폰트 패밀리
    isBold = false,          // 굵게
    color = "000000",        // 색상 (16진수)
    alignment = ParagraphAlignment.LEFT  // 정렬
)
```

### 테이블 테두리 스타일
```kotlin
// 사전 정의된 스타일
setBorders(TableBorderStyle.SINGLE)   // 실선
setBorders(TableBorderStyle.DOUBLE)   // 이중선
setBorders(TableBorderStyle.THICK)    // 굵은 선
setBorders(TableBorderStyle.DOTTED)   // 점선
setBorders(TableBorderStyle.DASHED)   // 파선
setBorders(TableBorderStyle.NONE)     // 테두리 없음

// 개별 테두리 설정
setBordersWithCells(
    top = TableBorderStyle.THICK,
    bottom = TableBorderStyle.SINGLE,
    left = TableBorderStyle.SINGLE,
    right = TableBorderStyle.SINGLE,
    insideH = TableBorderStyle.DOTTED,
    insideV = TableBorderStyle.DOTTED
)
```

### 마진 설정
```kotlin
margins {
    top = 1440        // twips 단위 (1/20 포인트)
    bottom = 1440
    left = 1800
    right = 1800
    
    // 또는 편의 메서드 사용
    allInches(1.0)         // 1인치
    allCentimeters(2.5)    // 2.5cm
}
```

## 📝 참고사항

### 단위 시스템
- **Twips**: Word 문서의 기본 단위 (1/20 포인트)
  - 1인치 = 1440 twips
  - 1cm = 567 twips
- **포인트**: 이미지 크기에 사용 (1/72 인치)

### 색상 코드
- 16진수 형식 사용: "FF0000" (빨강), "00FF00" (녹색), "0000FF" (파랑)
- 투명도는 지원하지 않음

### 폰트 지원
- 시스템에 설치된 폰트 사용 가능
- 기본 권장: "Pretendard", "Arial", "맑은 고딕"
- 한글 지원을 위해서는 한글 폰트 필수

### 이미지 형식
- PNG, JPEG, GIF 등 ImageIO가 지원하는 모든 형식
- BufferedImage 직접 삽입 지원
- 자동 리사이징으로 메모리 효율성 확보

### 성능 고려사항
- 대용량 이미지는 미리 리사이징 권장
- 복잡한 테이블은 메모리 사용량 증가
- 문서 생성 후 반드시 `close()` 호출

## 🔗 관련 문서

- [BMOA 원본 프로젝트](../../../../../../../BHC/BMOA/BMOA-api-server)
- [Platform Template 프로젝트 구조](../../../../../README.md)
- [Common 모듈 가이드](../../README.md)
- [이미지 처리 모듈](../image/README.md)

## 🛠️ 의존성

```kotlin
// build.gradle.kts
implementation("org.apache.poi:poi:5.2.4")
implementation("org.apache.poi:poi-ooxml:5.2.4")
implementation("org.apache.poi:poi-scratchpad:5.2.4")
```

## 📄 라이선스

Apache POI 라이선스를 따릅니다. 상업적 사용 가능.