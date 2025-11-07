package com.dailymemo.domain.models

data class PublicRoom(
    val roomId: Long,
    val roomName: String,
    val roomCode: String,
    val ownerName: String,
    val likesCount: Int,
    val membersCount: Int
)

data class PublicRoomsResult(
    val rooms: List<PublicRoom>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)
