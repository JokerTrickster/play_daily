package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.KakaoLocalApiService
import com.dailymemo.domain.models.Place
import com.dailymemo.domain.repositories.PlaceRepository
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val kakaoLocalApiService: KakaoLocalApiService
) : PlaceRepository {

    private val kakaoRestApiKey = "2aefa7ae1065ae55af45600c536c9471"

    override suspend fun searchPlaces(
        query: String,
        longitude: Double?,
        latitude: Double?,
        radius: Int
    ): Result<List<Place>> {
        return try {
            val response = kakaoLocalApiService.searchPlaces(
                authorization = "KakaoAK $kakaoRestApiKey",
                query = query,
                longitude = longitude,
                latitude = latitude,
                radius = radius
            )

            if (response.isSuccessful) {
                val places = response.body()?.documents?.map { dto ->
                    Place(
                        id = dto.id,
                        name = dto.placeName,
                        category = dto.categoryName,
                        phone = dto.phone,
                        address = dto.addressName,
                        roadAddress = dto.roadAddressName,
                        latitude = dto.latitude.toDouble(),
                        longitude = dto.longitude.toDouble(),
                        placeUrl = dto.placeUrl
                    )
                } ?: emptyList()
                Result.success(places)
            } else {
                Result.failure(Exception("Failed to search places: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchPlacesByCategory(
        categoryGroupCode: String,
        longitude: Double,
        latitude: Double,
        radius: Int
    ): Result<List<Place>> {
        return try {
            val response = kakaoLocalApiService.searchPlacesByCategory(
                authorization = "KakaoAK $kakaoRestApiKey",
                categoryGroupCode = categoryGroupCode,
                longitude = longitude,
                latitude = latitude,
                radius = radius
            )

            if (response.isSuccessful) {
                val places = response.body()?.documents?.map { dto ->
                    Place(
                        id = dto.id,
                        name = dto.placeName,
                        category = dto.categoryName,
                        phone = dto.phone,
                        address = dto.addressName,
                        roadAddress = dto.roadAddressName,
                        latitude = dto.latitude.toDouble(),
                        longitude = dto.longitude.toDouble(),
                        placeUrl = dto.placeUrl
                    )
                } ?: emptyList()
                Result.success(places)
            } else {
                Result.failure(Exception("Failed to search places by category: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
