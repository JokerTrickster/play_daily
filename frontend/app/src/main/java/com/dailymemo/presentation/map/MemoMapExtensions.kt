package com.dailymemo.presentation.map

import com.dailymemo.domain.models.Memo

/**
 * Memo의 상태(isPinned, isWishlist)에 따라 적절한 마커 핀 색상을 반환합니다.
 *
 * 우선순위:
 * 1. isPinned = true → Yellow (고정 메모가 최우선)
 * 2. isWishlist = true → Red (위시리스트)
 * 3. 기본 → Blue (방문한 장소)
 *
 * @return 마커 핀 아이콘 (Blue, Red, Yellow)
 */
fun Memo.getMarkerPin(): MarkerPinIcon {
    return when {
        isPinned -> MarkerPinIcon.Yellow
        isWishlist -> MarkerPinIcon.Red
        else -> MarkerPinIcon.Blue
    }
}

/**
 * Kakao Map SDK InfoWindow에 표시할 텍스트를 생성합니다.
 *
 * 포맷:
 * - 평점 있음: "{제목} | ⭐ {평점}"
 * - 평점 없음: "{제목}"
 *
 * 평점은 소수점 한 자리까지 표시 (예: 4.5)
 *
 * @return InfoWindow 텍스트
 */
fun Memo.toInfoWindowText(): String {
    val ratingText = if (rating > 0f) {
        " | ⭐ ${"%.1f".format(rating)}"
    } else {
        ""
    }
    return "$title$ratingText"
}

/**
 * Memo가 지도에 표시 가능한지 검증합니다.
 *
 * @return 위도/경도가 모두 null이 아닌 경우 true
 */
fun Memo.hasValidLocation(): Boolean {
    return latitude != null && longitude != null
}
