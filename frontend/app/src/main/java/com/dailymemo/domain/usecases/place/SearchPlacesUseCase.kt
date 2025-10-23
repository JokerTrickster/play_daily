package com.dailymemo.domain.usecases.place

import com.dailymemo.domain.models.Place
import com.dailymemo.domain.repositories.PlaceRepository
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(
        query: String,
        longitude: Double? = null,
        latitude: Double? = null,
        radius: Int = 300000  // 300km
    ): Result<List<Place>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return placeRepository.searchPlaces(query, longitude, latitude, radius)
    }
}
