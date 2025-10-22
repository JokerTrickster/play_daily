package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.repositories.MemoRepository
import javax.inject.Inject

class UpdateMemoUseCase @Inject constructor(
    private val repository: MemoRepository
) {
    suspend operator fun invoke(
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
        category: PlaceCategory? = null,
        isWishlist: Boolean = false,
        businessName: String? = null,
        businessPhone: String? = null,
        businessAddress: String? = null
    ): Result<Memo> {
        return repository.updateMemo(
            id = id,
            title = title,
            content = content,
            imageUrl = imageUrl,
            imageUrls = imageUrls,
            rating = rating,
            isPinned = isPinned,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            isWishlist = isWishlist,
            businessName = businessName,
            businessPhone = businessPhone,
            businessAddress = businessAddress
        )
    }
}
