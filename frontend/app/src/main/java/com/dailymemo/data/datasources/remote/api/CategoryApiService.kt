package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.remote.dto.CategoriesResponse
import retrofit2.Response
import retrofit2.http.GET

interface CategoryApiService {

    @GET("/v0.1/categories")
    suspend fun getCategories(): Response<CategoriesResponse>
}
