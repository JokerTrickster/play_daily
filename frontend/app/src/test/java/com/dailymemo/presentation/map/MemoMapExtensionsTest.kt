package com.dailymemo.presentation.map

import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.PlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class MemoMapExtensionsTest {

    private fun createTestMemo(
        isPinned: Boolean = false,
        isWishlist: Boolean = false,
        rating: Float = 0f,
        title: String = "테스트 장소",
        latitude: Double? = 37.5665,
        longitude: Double? = 126.9780
    ): Memo {
        return Memo(
            id = 1L,
            userId = 1L,
            title = title,
            content = "테스트 내용",
            imageUrl = null,
            rating = rating,
            isPinned = isPinned,
            latitude = latitude,
            longitude = longitude,
            locationName = "서울시청",
            category = PlaceCategory.RESTAURANT,
            isWishlist = isWishlist,
            businessName = null,
            businessPhone = null,
            businessAddress = null,
            naverPlaceUrl = null,
            comments = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `getMarkerPin - 고정 메모는 Yellow 핀 반환`() {
        val memo = createTestMemo(isPinned = true)
        assertEquals(MarkerPinIcon.Yellow, memo.getMarkerPin())
    }

    @Test
    fun `getMarkerPin - 위시리스트는 Red 핀 반환`() {
        val memo = createTestMemo(isWishlist = true)
        assertEquals(MarkerPinIcon.Red, memo.getMarkerPin())
    }

    @Test
    fun `getMarkerPin - 일반 메모는 Blue 핀 반환`() {
        val memo = createTestMemo()
        assertEquals(MarkerPinIcon.Blue, memo.getMarkerPin())
    }

    @Test
    fun `getMarkerPin - 고정 메모가 위시리스트보다 우선순위 높음`() {
        val memo = createTestMemo(isPinned = true, isWishlist = true)
        assertEquals(MarkerPinIcon.Yellow, memo.getMarkerPin())
    }

    @Test
    fun `toInfoWindowText - 평점 있을 경우 제목과 평점 표시`() {
        val memo = createTestMemo(rating = 4.5f, title = "맛집")
        assertEquals("맛집 | ⭐ 4.5", memo.toInfoWindowText())
    }

    @Test
    fun `toInfoWindowText - 평점 없을 경우 제목만 표시`() {
        val memo = createTestMemo(rating = 0f, title = "카페")
        assertEquals("카페", memo.toInfoWindowText())
    }

    @Test
    fun `toInfoWindowText - 평점 소수점 한 자리 포맷`() {
        val memo = createTestMemo(rating = 4.567f, title = "레스토랑")
        assertEquals("레스토랑 | ⭐ 4.6", memo.toInfoWindowText())
    }

    @Test
    fun `toInfoWindowText - 평점 0점도 평점 없음으로 처리`() {
        val memo = createTestMemo(rating = 0f)
        assertFalse(memo.toInfoWindowText().contains("⭐"))
    }

    @Test
    fun `hasValidLocation - 위도 경도 모두 있으면 true`() {
        val memo = createTestMemo(latitude = 37.5665, longitude = 126.9780)
        assertTrue(memo.hasValidLocation())
    }

    @Test
    fun `hasValidLocation - 위도 없으면 false`() {
        val memo = createTestMemo(latitude = null, longitude = 126.9780)
        assertFalse(memo.hasValidLocation())
    }

    @Test
    fun `hasValidLocation - 경도 없으면 false`() {
        val memo = createTestMemo(latitude = 37.5665, longitude = null)
        assertFalse(memo.hasValidLocation())
    }

    @Test
    fun `hasValidLocation - 둘 다 없으면 false`() {
        val memo = createTestMemo(latitude = null, longitude = null)
        assertFalse(memo.hasValidLocation())
    }
}
