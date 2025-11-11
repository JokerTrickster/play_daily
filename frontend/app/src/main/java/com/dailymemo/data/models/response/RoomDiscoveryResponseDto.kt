package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Room discovery list item DTO matching backend response
 * Backend: ResRoomListItem in backend/src/features/room/model/response/roomList.go
 */
data class RoomDiscoveryItemDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("room_code")
    val roomCode: String,

    @SerializedName("is_public")
    val isPublic: Boolean,

    @SerializedName("likes_count")
    val likesCount: Long,

    @SerializedName("owner_id")
    val ownerId: Long,

    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Paginated room discovery list response DTO matching backend response
 * Backend: ResRoomList in backend/src/features/room/model/response/roomList.go
 */
data class RoomDiscoveryResponseDto(
    @SerializedName("rooms")
    val rooms: List<RoomDiscoveryItemDto>,

    @SerializedName("total")
    val total: Long,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("has_next")
    val hasNext: Boolean
)
