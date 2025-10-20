package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.Place

interface PlaceRepository {
    suspend fun searchPlaces(
        query: String,
        longitude: Double? = null,
        latitude: Double? = null,
        radius: Int = 2000
    ): Result<List<Place>>

    suspend fun searchPlacesByCategory(
        categoryGroupCode: String,
        longitude: Double,
        latitude: Double,
        radius: Int = 2000
    ): Result<List<Place>>
}
