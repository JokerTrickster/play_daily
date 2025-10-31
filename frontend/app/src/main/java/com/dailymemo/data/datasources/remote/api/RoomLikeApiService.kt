package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.response.LikedRoomResponseDto
import com.dailymemo.data.models.response.RoomLikeStatusResponseDto
import retrofit2.Response
import retrofit2.http.*

interface RoomLikeApiService {

    @POST("/v0.1/room/{room_id}/like")
    suspend fun likeRoom(
        @Path("room_id") roomId: Long
    ): Response<Unit>

    @DELETE("/v0.1/room/{room_id}/like")
    suspend fun unlikeRoom(
        @Path("room_id") roomId: Long
    ): Response<Unit>

    @GET("/v0.1/room/liked")
    suspend fun getLikedRooms(): Response<List<LikedRoomResponseDto>>

    @GET("/v0.1/room/{room_id}/like/status")
    suspend fun getRoomLikeStatus(
        @Path("room_id") roomId: Long
    ): Response<RoomLikeStatusResponseDto>
}
