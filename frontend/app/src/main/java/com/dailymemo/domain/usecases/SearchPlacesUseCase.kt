package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.Place
import com.dailymemo.domain.repositories.PlaceRepository
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(
        query: String,
        longitude: Double?,
        latitude: Double?,
        radius: Int = 2000
    ): Result<List<Place>> {
        return placeRepository.searchPlaces(query, longitude, latitude, radius)
    }
}
