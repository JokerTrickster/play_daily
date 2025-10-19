package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.request.CreateMemoRequestDto
import com.dailymemo.data.models.request.UpdateMemoRequestDto
import com.dailymemo.data.models.response.MemoDto
import com.dailymemo.data.models.response.MemoListDto
import retrofit2.Response
import retrofit2.http.*

interface MemoApiService {

    @GET("/v0.1/memo")
    suspend fun getMemos(): Response<MemoListDto>

    @GET("/v0.1/memo/{id}")
    suspend fun getMemo(
        @Path("id") id: Long
    ): Response<MemoDto>

    @POST("/v0.1/memo")
    suspend fun createMemo(
        @Body request: CreateMemoRequestDto
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
