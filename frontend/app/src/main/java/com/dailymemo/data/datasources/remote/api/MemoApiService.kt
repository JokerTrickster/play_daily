package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.request.CreateMemoRequestDto
import com.dailymemo.data.models.request.UpdateMemoRequestDto
import com.dailymemo.data.models.response.MemoDto
import com.dailymemo.data.models.response.MemoListDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface MemoApiService {

    @GET("/v0.1/memo")
    suspend fun getMemos(
        @Query("is_wishlist") isWishlist: Boolean? = null
    ): Response<MemoListDto>

    @GET("/v0.1/memo/{id}")
    suspend fun getMemo(
        @Path("id") id: Long
    ): Response<MemoDto>

    @Multipart
    @POST("/v0.1/memo")
    suspend fun createMemo(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody?,
        @Part("rating") rating: RequestBody?,
        @Part("is_pinned") isPinned: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("location_name") locationName: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("is_wishlist") isWishlist: RequestBody?,
        @Part("business_name") businessName: RequestBody?,
        @Part("business_phone") businessPhone: RequestBody?,
        @Part("business_address") businessAddress: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<MemoDto>

    @PUT("/v0.1/memo/{id}")
    suspend fun updateMemo(
        @Path("id") id: Long,
        @Body request: UpdateMemoRequestDto
    ): Response<MemoDto>

    @DELETE("/v0.1/memo/{id}")
    suspend fun deleteMemo(
        @Path("id") id: Long
    ): Response<Unit>
}
