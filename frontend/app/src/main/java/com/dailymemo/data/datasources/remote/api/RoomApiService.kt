package com.dailymemo.data.datasources.remote.api

import com.dailymemo.data.models.request.JoinRoomRequestDto
import com.dailymemo.data.models.request.KickUserRequestDto
import com.dailymemo.data.models.request.UpdateRoomPrivacyRequestDto
import com.dailymemo.data.models.response.JoinRoomResponseDto
import com.dailymemo.data.models.response.PublicRoomsResponseDto
import com.dailymemo.data.models.response.RoomMembersResponseDto
import com.dailymemo.data.models.response.UpdateRoomPrivacyResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

    @POST("/v0.1/room/permission")
    suspend fun updateMemberPermission(
        @Body request: Map<String, Any>
    ): Response<Map<String, String>>

    @PUT("/v0.1/rooms/{room_id}/privacy")
    suspend fun updateRoomPrivacy(
        @Path("room_id") roomId: Long,
        @Body request: UpdateRoomPrivacyRequestDto
    ): Response<UpdateRoomPrivacyResponseDto>

    @GET("/v0.1/rooms/public")
    suspend fun getPublicRooms(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<PublicRoomsResponseDto>

    @POST("/v0.1/rooms/join")
    suspend fun joinRoomByCode(
        @Body request: JoinRoomRequestDto
    ): Response<JoinRoomResponseDto>
}
