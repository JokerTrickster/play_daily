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
        @Query("is_wishlist") isWishlist: Boolean? = null,
        @Query("room_id") roomId: Int? = null,
        @Query("category_ids") categoryIds: List<Int>? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
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
        @Part("creation_mode") creationMode: RequestBody, // NEW
        @Part("category_ids") categoryIds: RequestBody, // NEW
        @Part("rating") rating: RequestBody?,
        @Part("is_pinned") isPinned: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("location_name") locationName: RequestBody?,
        @Part("category") category: RequestBody?, // Deprecated
        @Part("is_wishlist") isWishlist: RequestBody?,
        @Part("business_name") businessName: RequestBody?,
        @Part("business_phone") businessPhone: RequestBody?,
        @Part("business_address") businessAddress: RequestBody?,
        @Part("naver_place_url") naverPlaceUrl: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<MemoDto>

    @Multipart
    @PUT("/v0.1/memo/{id}")
    suspend fun updateMemo(
        @Path("id") id: Long,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody?,
        @Part("rating") rating: RequestBody?,
        @Part("is_pinned") isPinned: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("location_name") locationName: RequestBody?,
        @Part("is_wishlist") isWishlist: RequestBody?,
        @Part("business_name") businessName: RequestBody?,
        @Part("business_phone") businessPhone: RequestBody?,
        @Part("business_address") businessAddress: RequestBody?,
        @Part("category_ids") categoryIds: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<MemoDto>

    @DELETE("/v0.1/memo/{id}")
    suspend fun deleteMemo(
        @Path("id") id: Long
    ): Response<Unit>

    @POST("/v0.1/memo/{id}/like")
    suspend fun toggleMemoLike(
        @Path("id") id: Long
    ): Response<Map<String, Any>>
}
