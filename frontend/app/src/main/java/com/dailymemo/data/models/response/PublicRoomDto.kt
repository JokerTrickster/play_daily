package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

data class PublicRoomDto(
    @SerializedName("room_id")
    val roomId: Long,

    @SerializedName("room_name")
    val roomName: String,

    @SerializedName("room_code")
    val roomCode: String,

    @SerializedName("owner_name")
    val ownerName: String,

    @SerializedName("likes_count")
    val likesCount: Int,

    @SerializedName("members_count")
    val membersCount: Int
)

data class PublicRoomsResponseDto(
    @SerializedName("rooms")
    val rooms: List<PublicRoomDto>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("has_more")
    val hasMore: Boolean
)
