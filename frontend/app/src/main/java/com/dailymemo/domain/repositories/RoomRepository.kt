package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.RoomMember

interface RoomRepository {
    suspend fun joinRoom(roomId: Long, roomPassword: String): Result<Unit>
    suspend fun kickUser(roomId: Long, targetUserId: Long): Result<Unit>
    suspend fun getRoomMembers(roomId: Long): Result<List<RoomMember>>
}
