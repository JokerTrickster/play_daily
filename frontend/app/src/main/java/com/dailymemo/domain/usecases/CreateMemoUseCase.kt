package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.repositories.MemoRepository
import javax.inject.Inject

class CreateMemoUseCase @Inject constructor(
    private val repository: MemoRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        imageUri: android.net.Uri? = null,
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
        businessAddress: String? = null,
        naverPlaceUrl: String? = null
    ): Result<Memo> {
        return repository.createMemo(
            title = title,
            content = content,
            imageUri = imageUri,
            imageUrls = imageUrls,
            rating = rating,
            isPinned = isPinned,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            category = category,
            isWishlist = isWishlist,
            businessName = businessName,
            businessPhone = businessPhone,
            businessAddress = businessAddress,
            naverPlaceUrl = naverPlaceUrl
        )
    }
}
