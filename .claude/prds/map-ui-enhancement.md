---
name: map-ui-enhancement
description: 카카오맵 마커 및 정보 표시 UI/UX 개선 - 시각적 핀 디자인과 인터랙티브 팝업
status: backlog
created: 2025-10-22T03:11:29Z
---

# PRD: 카카오맵 UI/UX 개선

## Executive Summary

현재 play_daily 앱의 지도 화면에서 사용하는 별 모양 마커는 시각적으로 구분이 어렵고, 장소 정보를 확인하기 위해 별도의 액션이 필요합니다. 이 개선안은 직관적인 핀 디자인과 인터랙티브 정보 팝업을 도입하여 사용자 경험을 향상시키고, 한눈에 장소의 주요 정보(이름, 평점)를 파악할 수 있도록 합니다.

**핵심 가치:**
- 시각적으로 명확한 색상 기반 마커 (방문한 곳 vs 위시리스트 vs 고정 메모)
- 2단계 인터랙션으로 빠른 정보 확인 및 상세 탐색
- 평점과 장소명을 포함한 인포윈도우로 의사결정 지원

## Problem Statement

### 현재 문제점
1. **시각적 구분 불가**: 별 모양 마커로는 방문한 곳과 위시리스트를 구분하기 어려움
2. **정보 접근성 낮음**: 장소명, 평점 등 핵심 정보를 보려면 매번 메모 상세로 진입해야 함
3. **사용자 경험 저하**: 지도에서 여러 장소를 비교하거나 탐색할 때 비효율적

### 왜 지금 중요한가?
- 사용자가 메모를 쌓을수록 지도 화면이 주요 탐색 도구가 됨
- 위시리스트 기능 추가로 방문 전/후 장소를 구분할 필요성 증가
- 평점 시스템(0.5 단위) 도입으로 장소 선택 시 평점 정보가 중요해짐

## User Stories

### Primary Personas

**페르소나 1: 맛집 탐험가 지수 (28세)**
- 새로운 카페와 맛집을 찾아다니며 기록하는 것을 좋아함
- 다음 방문 장소를 결정할 때 평점과 위치를 동시에 고려
- 주말마다 2-3곳의 새로운 장소를 방문하고 메모 작성

**페르소나 2: 여행 플래너 민준 (32세)**
- 여행 전 가고 싶은 곳을 위시리스트에 저장
- 지도에서 동선을 확인하며 방문 순서 계획
- 방문 후 평점과 메모를 추가하여 나중에 재방문 결정

### User Journeys

#### Journey 1: 주말 외출 장소 결정하기
```
1. 지수가 지도 화면을 열어 주변 저장된 장소들을 확인
2. 파란색 핀(방문한 곳)과 빨간색 핀(위시리스트)을 한눈에 구분
3. 빨간색 핀을 탭하여 [카페 이름 | ⭐ 4.5] 정보 확인
4. 여러 위시리스트 장소의 평점을 비교
5. 가장 평점이 높은 카페의 팝업을 다시 탭하여 상세 정보 확인
6. 메모 상세 화면에서 메뉴, 사진, 주소 등을 확인하고 방문 결정
```

#### Journey 2: 여행 동선 계획하기
```
1. 민준이 제주도 여행을 위해 저장한 위시리스트 지도 확인
2. 빨간색 핀들의 위치와 거리 파악
3. 각 핀을 탭하여 장소명과 평점 빠르게 스캔
4. 동선상 가까운 곳들을 묶어 하루 일정 계획
5. 노란색 핀(고정 메모)으로 숙소 위치 확인
6. 최적의 이동 경로 결정
```

### Pain Points Being Addressed

| 기존 문제 | 개선 후 |
|---------|---------|
| 모든 마커가 별 모양으로 동일 | 색상으로 즉시 구분 (파란/빨간/노란) |
| 장소 정보 확인을 위해 매번 상세 진입 | 탭 한 번으로 이름과 평점 확인 |
| 여러 장소 비교 시 반복적인 클릭 | 팝업으로 빠른 스캔 가능 |
| 이미지가 없으면 장소 기억 어려움 | 팝업에 썸네일 표시 |

## Requirements

### Functional Requirements

#### FR-1: 색상 기반 마커 시스템
- **FR-1.1**: 방문한 장소는 파란색 핀 마커로 표시
- **FR-1.2**: 위시리스트 장소는 빨간색 핀 마커로 표시
- **FR-1.3**: 고정된 메모는 노란색 핀 마커로 표시
- **FR-1.4**: 핀은 Google Maps 스타일의 둥근 상단 + 뾰족한 하단 디자인
- **FR-1.5**: 핀 크기는 일관되게 유지 (줌 레벨과 무관)

#### FR-2: 인터랙티브 정보 팝업
- **FR-2.1**: 마커 탭 시 장소명과 평점이 포함된 인포윈도우 표시
- **FR-2.2**: 인포윈도우 형식: `[장소명 | ⭐ 4.5]`
- **FR-2.3**: 평점이 0인 경우 평점 표시 생략
- **FR-2.4**: 인포윈도우는 마커 위쪽에 말풍선 형태로 표시
- **FR-2.5**: 다른 마커 탭 시 기존 인포윈도우는 자동으로 닫힘

#### FR-3: 상세 정보 팝업 (확장된 인포윈도우)
- **FR-3.1**: 인포윈도우 탭 시 확장된 팝업 카드 표시
- **FR-3.2**: 팝업 카드 내용:
  - 장소명 (제목)
  - 평점 (⭐ 아이콘 + 숫자)
  - 대표 이미지 썸네일 (있는 경우, 최대 1장)
  - 카테고리 아이콘 (🍽️ 음식점, ☕ 카페 등)
  - 메모 생성일 또는 방문일
- **FR-3.3**: 팝업 카드 하단에 "상세 보기" 버튼
- **FR-3.4**: "상세 보기" 버튼 탭 시 메모 상세 화면으로 이동
- **FR-3.5**: 팝업 외부 영역 탭 시 팝업 닫힘

#### FR-4: 마커 표시 로직
- **FR-4.1**: 모든 마커를 항상 표시 (클러스터링 없음)
- **FR-4.2**: 지도 이동/줌 시에도 모든 마커 유지
- **FR-4.3**: 현재 보이는 영역의 마커만 렌더링 (성능 최적화)
- **FR-4.4**: 마커 로딩 시 페이드인 애니메이션 적용

#### FR-5: 카테고리 아이콘 매핑
```
FOOD: 🍽️
CAFE: ☕
CULTURE: 🎭
ACCOMMODATION: 🏨
SHOPPING: 🛍️
ACTIVITY: 🎯
OTHER: 📍
```

### Non-Functional Requirements

#### NFR-1: 성능
- **NFR-1.1**: 100개 마커 렌더링 시 60fps 유지
- **NFR-1.2**: 마커 탭 후 인포윈도우 표시까지 100ms 이내
- **NFR-1.3**: 팝업 카드 로딩 시간 200ms 이내

#### NFR-2: 접근성
- **NFR-2.1**: 색맹 사용자를 위해 색상 외 시각적 구분 요소 추가 (핀 내부 아이콘)
- **NFR-2.2**: TalkBack 지원 (마커 설명: "방문한 장소, [장소명], 평점 4.5점")
- **NFR-2.3**: 최소 터치 영역 48x48dp 보장

#### NFR-3: 디자인 일관성
- **NFR-3.1**: Material Design 3 가이드라인 준수
- **NFR-3.2**: 앱 전체 컬러 시스템과 조화
- **NFR-3.3**: 다크 모드 지원 (핀 색상 밝기 조정)

#### NFR-4: 호환성
- **NFR-4.1**: Android 8.0(API 26) 이상 지원
- **NFR-4.2**: 다양한 화면 크기 대응 (폰, 태블릿)
- **NFR-4.3**: Kakao Map SDK 최신 버전 호환

## Success Criteria

### Measurable Outcomes

#### Primary Metrics
1. **사용자 만족도**
   - 목표: 지도 화면 평균 체류 시간 30% 증가
   - 측정: Firebase Analytics 이벤트 트래킹

2. **인터랙션 효율성**
   - 목표: 메모 상세 진입률 50% 감소 (정보를 팝업에서 확인)
   - 측정: 인포윈도우 탭 vs 상세 화면 진입 비율

3. **정보 탐색 속도**
   - 목표: 평균 5개 장소 탐색 시간 40% 단축
   - 측정: 사용자 테스트 태스크 완료 시간

#### Secondary Metrics
- 지도 화면 DAU(Daily Active Users) 20% 증가
- 위시리스트 활용률 35% 증가
- 앱 스토어 리뷰 평점 0.3점 상승

### Key Performance Indicators (KPIs)

| KPI | 현재 (예상) | 목표 | 측정 방법 |
|-----|------------|------|----------|
| 지도 화면 체류 시간 | 45초 | 60초 | Firebase Analytics |
| 인포윈도우 오픈율 | - | 80% | 이벤트 로그 |
| 팝업 카드 오픈율 | - | 40% | 이벤트 로그 |
| 상세 화면 진입율 | 90% | 45% | 화면 전환 추적 |
| 마커 렌더링 성능 | - | >60fps | 성능 프로파일링 |

## Constraints & Assumptions

### Technical Constraints
1. **Kakao Map SDK 제약**
   - 커스텀 마커 이미지는 PNG/SVG 지원
   - 인포윈도우는 제한된 HTML 태그만 지원 가능
   - 동시에 1개 인포윈도우만 표시 가능

2. **성능 제약**
   - 1000개 이상 마커 시 성능 저하 가능성
   - 복잡한 팝업 카드는 메모리 사용량 증가

3. **플랫폼 제약**
   - Android 전용 (iOS는 별도 구현 필요)
   - 네이티브 Compose UI로 구현

### Assumptions
1. **사용자 행동**
   - 사용자는 색상으로 마커 타입을 빠르게 학습함
   - 2단계 인터랙션(탭→팝업→상세)이 직관적임
   - 평점 정보가 의사결정에 중요한 역할을 함

2. **데이터**
   - 모든 메모에 위치 정보(위도/경도)가 있음
   - 평점은 0~5.0 범위의 Float 값
   - 이미지는 URL로 제공되며 썸네일 생성 가능

3. **비즈니스**
   - 이 기능은 사용자 리텐션 증가에 기여
   - 지도 기능은 앱의 핵심 가치 중 하나
   - 향후 위치 기반 추천 기능으로 확장 가능

### Timeline Constraints
- **Phase 1 (2주)**: 기본 마커 색상 구분 + 간단한 인포윈도우
- **Phase 2 (2주)**: 확장 팝업 카드 + 상세 정보 표시
- **Phase 3 (1주)**: 성능 최적화 + 애니메이션 polish

## Out of Scope

명시적으로 이번 개선에서 **제외**되는 항목:

1. **클러스터링**: 마커가 많이 겹쳐도 모든 마커를 개별 표시
2. **경로 탐색**: 두 장소 간 길찾기 기능
3. **필터링 UI**: 카테고리별 마커 show/hide 토글
4. **커스텀 마커 디자인**: 사용자가 마커 모양/색상 변경
5. **오프라인 지도**: 인터넷 없이 지도 사용
6. **실시간 위치 공유**: 친구와 위치 공유 기능
7. **AR 모드**: 증강현실 기반 장소 탐색
8. **다중 선택**: 여러 마커 동시 선택/조작
9. **마커 드래그**: 마커 위치 변경 기능
10. **줌 레벨별 정보 변경**: 모든 줌에서 동일한 정보 표시

## Dependencies

### External Dependencies

1. **Kakao Map SDK**
   - 최신 버전 (현재 2.x 이상)
   - 커스텀 마커 API
   - InfoWindow API
   - 지원: Kakao Developers 문서

2. **Coil (이미지 로딩)**
   - 버전 2.4.0+
   - 썸네일 이미지 로딩 및 캐싱
   - 의존: 현재 프로젝트에서 사용 중

3. **Material Design 3**
   - Compose Material3 라이브러리
   - 색상 시스템 및 컴포넌트

### Internal Dependencies

1. **Domain Layer**
   - `Memo` 모델 (위치, 평점, 카테고리, 이미지 URL)
   - `MemoRepository` (메모 데이터 조회)
   - `GetMemosUseCase` (필터링: isWishlist)

2. **Data Layer**
   - 위치 정보가 있는 메모만 필터링하는 쿼리 필요
   - 이미지 URL → 썸네일 변환 로직 (선택적)

3. **Presentation Layer**
   - `MapViewModel` 수정 필요
   - Navigation: 지도 화면 → 메모 상세 화면

4. **Design System**
   - 앱 컬러 팔레트 정의 필요
   - 핀 아이콘 에셋 (Blue, Red, Yellow Pin SVG)
   - 카테고리 아이콘 이모지 또는 벡터 에셋

### Team Dependencies

1. **디자인팀**
   - 핀 디자인 에셋 제공 (SVG/PNG)
   - 팝업 카드 UI 디자인 가이드
   - 다크모드 색상 정의

2. **백엔드팀**
   - 이미지 썸네일 생성 API (선택적)
   - 메모 조회 API 성능 최적화 (많은 마커 로딩 시)

3. **QA팀**
   - 다양한 디바이스 테스트 (화면 크기, 성능)
   - 접근성 테스트 (TalkBack, 색맹 모드)

## Technical Approach

### Architecture

```
MapScreen (Composable)
├── MapViewModel
│   ├── State: markers, selectedMarker, showPopup
│   └── Events: onMarkerClick, onInfoWindowClick, onPopupDismiss
├── KakaoMapView (AndroidView)
│   ├── CustomMarkers (colored pins)
│   └── InfoWindow (simple name + rating)
└── MemoPopupCard (Composable)
    ├── Image (Coil)
    ├── Title, Rating, Category
    └── DetailButton → Navigation
```

### Data Flow

```
1. MapViewModel.loadMarkers()
   → GetMemosUseCase (filter: hasLocation)
   → State.markers 업데이트

2. User taps marker
   → KakaoMap onMarkerClick
   → ViewModel.onMarkerClick(memoId)
   → State.selectedMarker 업데이트
   → InfoWindow 표시

3. User taps InfoWindow
   → KakaoMap onInfoWindowClick
   → ViewModel.onInfoWindowClick()
   → State.showPopup = true
   → MemoPopupCard 표시

4. User taps "상세 보기"
   → Navigation.navigate(MemoDetail, memoId)
```

### Key Implementation Details

1. **Marker Color Mapping**
```kotlin
fun Memo.toMarkerColor(): MarkerColor {
    return when {
        isPinned -> MarkerColor.YELLOW
        isWishlist -> MarkerColor.RED
        else -> MarkerColor.BLUE
    }
}
```

2. **InfoWindow Content**
```kotlin
fun Memo.toInfoWindowContent(): String {
    val ratingText = if (rating > 0) " | ⭐ $rating" else ""
    return "$title$ratingText"
}
```

3. **Popup Card Data**
```kotlin
data class MemoPopupData(
    val id: Long,
    val title: String,
    val rating: Float,
    val categoryIcon: String,
    val imageUrl: String?,
    val date: String
)
```

## Testing Strategy

### Unit Tests
- ViewModel 로직 테스트 (마커 로딩, 선택, 팝업 상태)
- 색상 매핑 함수 테스트
- InfoWindow 콘텐츠 생성 테스트

### Integration Tests
- Kakao Map SDK 연동 테스트
- Navigation 테스트 (팝업 → 상세 화면)

### UI Tests
- 마커 클릭 → 인포윈도우 표시
- 인포윈도우 클릭 → 팝업 카드 표시
- 팝업 "상세 보기" → 화면 전환

### Performance Tests
- 100개 마커 렌더링 성능
- 스크롤/줌 시 프레임률 측정

### Acceptance Tests
- 색맹 모드에서 마커 구분 가능 여부
- TalkBack으로 마커 정보 읽기
- 다양한 화면 크기에서 UI 정상 표시

## Rollout Plan

### Phase 1: MVP (Week 1-2)
- 색상별 핀 마커 구현
- 기본 인포윈도우 ([장소명 | ⭐ 평점])
- 내부 베타 테스트

### Phase 2: Enhanced UX (Week 3-4)
- 확장 팝업 카드 구현
- 썸네일 이미지 표시
- 카테고리 아이콘 추가
- A/B 테스트 준비

### Phase 3: Polish (Week 5)
- 애니메이션 추가
- 성능 최적화
- 접근성 개선
- 정식 출시

### Rollback Plan
- Feature Flag로 신규 UI/기존 UI 전환 가능
- 문제 발생 시 기존 별 마커로 즉시 롤백
- 사용자 피드백 수집 후 점진적 개선

## Success Metrics Dashboard

### 주간 모니터링 지표
```
┌─────────────────────────────────────┐
│ 지도 화면 KPI Dashboard             │
├─────────────────────────────────────┤
│ • 평균 체류 시간: [45s → 60s]      │
│ • 인포윈도우 오픈율: [80%]         │
│ • 팝업 카드 오픈율: [40%]          │
│ • 상세 화면 진입율: [90% → 45%]   │
│ • 마커 렌더링 성능: [60fps]        │
└─────────────────────────────────────┘
```

## Appendix

### Glossary
- **핀 마커**: 지도 위에 표시되는 위치 표시자 (Pin Marker)
- **인포윈도우**: 마커 탭 시 나타나는 간단한 정보 창
- **팝업 카드**: 인포윈도우 탭 시 확장되는 상세 정보 카드
- **클러스터링**: 겹치는 마커를 그룹화하여 하나의 마커로 표시

### References
- [Kakao Map SDK Documentation](https://apis.map.kakao.com/android/)
- [Material Design 3 - Maps](https://m3.material.io/)
- [Google Maps UX Best Practices](https://developers.google.com/maps/documentation/android-sdk/ux-best-practices)

### Design Assets Needed
1. Blue Pin Icon (48x48dp, SVG)
2. Red Pin Icon (48x48dp, SVG)
3. Yellow Pin Icon (48x48dp, SVG)
4. Category Icons Set (Emoji 또는 SVG)
5. Popup Card Background (Material Surface)

---

**문서 버전**: 1.0
**최종 수정일**: 2025-10-22
**작성자**: Product Manager
**검토자**: Engineering Lead, Design Lead
