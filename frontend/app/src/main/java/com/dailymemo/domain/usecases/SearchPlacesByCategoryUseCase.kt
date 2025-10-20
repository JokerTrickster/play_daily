package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.Place
import com.dailymemo.domain.models.PlaceCategory
import com.dailymemo.domain.repositories.PlaceRepository
import javax.inject.Inject

class SearchPlacesByCategoryUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(
        category: PlaceCategory,
        longitude: Double,
        latitude: Double,
        radius: Int = 1000
    ): Result<List<Place>> {
        // Use kakaoCode from PlaceCategory enum
        val categoryCode = category.kakaoCode ?: return Result.success(emptyList())

        return placeRepository.searchPlacesByCategory(categoryCode, longitude, latitude, radius)
    }
}
