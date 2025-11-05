package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.request.JoinRoomRequestDto
import com.dailymemo.data.models.request.KickUserRequestDto
import com.dailymemo.data.models.response.JoinRoomResponseDto
import com.dailymemo.data.models.response.RoomMembersResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RoomApiService {

    @POST("/v0.1/room/join")
    suspend fun joinRoom(
        @Body request: JoinRoomRequestDto
    ): Response<JoinRoomResponseDto>

    @POST("/v0.1/room/kick")
    suspend fun kickUser(
        @Body request: KickUserRequestDto
    ): Response<Map<String, String>>

    @GET("/v0.1/room/members")
    suspend fun getRoomMembers(
        @Query("room_id") roomId: Long
    ): Response<RoomMembersResponseDto>
}
