package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.response.KakaoPlaceSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalApiService {

    @GET("v2/local/search/keyword.json")
    suspend fun searchPlaces(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("x") longitude: Double? = null,
        @Query("y") latitude: Double? = null,
        @Query("radius") radius: Int = 2000,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15
    ): Response<KakaoPlaceSearchResponse>

    @GET("v2/local/search/category.json")
    suspend fun searchPlacesByCategory(
        @Header("Authorization") authorization: String,
        @Query("category_group_code") categoryGroupCode: String,
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("radius") radius: Int = 2000,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15
    ): Response<KakaoPlaceSearchResponse>
}
