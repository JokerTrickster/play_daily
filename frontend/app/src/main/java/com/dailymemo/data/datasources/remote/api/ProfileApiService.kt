package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.remote.dto.ProfileResponse
import com.dailymemo.data.remote.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {

    @GET("/v0.1/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("/v0.1/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>
}
