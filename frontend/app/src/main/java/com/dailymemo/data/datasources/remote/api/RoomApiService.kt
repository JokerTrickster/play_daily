package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.request.JoinRoomRequestDto
import com.dailymemo.data.models.response.JoinRoomResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RoomApiService {

    @POST("/v0.1/room/join")
    suspend fun joinRoom(
        @Body request: JoinRoomRequestDto
    ): Response<JoinRoomResponseDto>
}
