package com.dailymemo.data.models.response

data class LikedRoomResponseDto(
    val room_id: Long,
    val room_code: String,
    val room_name: String,
    val owner_id: Long,
    val likes_count: Int,
    val liked_at: String
)

data class RoomLikeStatusResponseDto(
    val is_liked: Boolean,
    val likes_count: Int
)
