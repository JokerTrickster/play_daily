package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.Memo
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
        rating: Int = 0,
        isPinned: Boolean = false,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null
    ): Result<Memo> {
        return repository.updateMemo(
            id, title, content, imageUrl, imageUrls, rating, isPinned,
            latitude, longitude, locationName
        )
    }
}
