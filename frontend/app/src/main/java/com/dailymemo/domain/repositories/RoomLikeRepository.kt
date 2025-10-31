package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.LikedRoom
import com.dailymemo.domain.models.RoomLikeStatus

interface RoomLikeRepository {
    suspend fun likeRoom(roomId: Long): Result<Unit>
    suspend fun unlikeRoom(roomId: Long): Result<Unit>
    suspend fun getLikedRooms(): Result<List<LikedRoom>>
    suspend fun getRoomLikeStatus(roomId: Long): Result<RoomLikeStatus>
}
