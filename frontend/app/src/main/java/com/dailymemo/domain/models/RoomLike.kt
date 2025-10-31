package com.dailymemo.domain.models

import java.time.LocalDateTime

data class RoomLike(
    val id: Long,
    val roomId: Long,
    val userId: Long,
    val createdAt: LocalDateTime
)

data class LikedRoom(
    val roomId: Long,
    val roomCode: String,
    val roomName: String,
    val ownerId: Long,
    val likesCount: Int,
    val likedAt: LocalDateTime
)

data class RoomLikeStatus(
    val isLiked: Boolean,
    val likesCount: Int
)
