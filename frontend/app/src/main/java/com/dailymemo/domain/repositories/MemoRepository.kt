package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.CreationMode
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.MemoListResult
import com.dailymemo.domain.models.PlaceCategory

interface MemoRepository {
    suspend fun getMemos(
        isWishlist: Boolean? = null,
        roomId: Int? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<MemoListResult>
    suspend fun getMemo(id: Long): Result<Memo>
    suspend fun createMemo(
        title: String,
        content: String = "", // Deprecated
        creationMode: CreationMode = CreationMode.LIST, // NEW
        categoryIds: List<Int> = emptyList(), // NEW
        imageUri: android.net.Uri? = null,
        imageUrls: List<String> = emptyList(),
        rating: Float = 0f,
        isPinned: Boolean = false,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null,
        category: PlaceCategory? = null, // Deprecated
        isWishlist: Boolean = false,
        businessName: String? = null,
        businessPhone: String? = null,
        businessAddress: String? = null,
        naverPlaceUrl: String? = null
    ): Result<Memo>
    suspend fun updateMemo(
        id: Long,
        title: String,
        content: String,
        imageUrl: String? = null,
        imageUrls: List<String> = emptyList(),
        rating: Float = 0f,
        isPinned: Boolean = false,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null,
        isWishlist: Boolean = false,
        businessName: String? = null,
        businessPhone: String? = null,
        businessAddress: String? = null
    ): Result<Memo>
    suspend fun deleteMemo(id: Long): Result<Unit>
    suspend fun uploadImage(imageUri: android.net.Uri): Result<String> // 이미지 업로드 후 URL 반환
    suspend fun toggleMemoLike(memoId: Long): Result<Boolean> // 좋아요 토글, 반환값은 좋아요 상태 (true=liked, false=unliked)
}
