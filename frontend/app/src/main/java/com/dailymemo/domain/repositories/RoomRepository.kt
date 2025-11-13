package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.RoomMember
import com.dailymemo.domain.models.RoomPermission
import com.dailymemo.presentation.room.RoomCardData
import com.dailymemo.presentation.room.RoomDetail

interface RoomRepository {
    suspend fun joinRoom(roomId: Long, roomPassword: String): Result<Unit>
    suspend fun kickUser(roomId: Long, targetUserId: Long): Result<Unit>
    suspend fun getRoomMembers(roomId: Long): Result<List<RoomMember>>
    suspend fun updateMemberPermission(roomId: Long, userId: Long, permission: RoomPermission): Result<Unit>
    suspend fun updateRoomSettings(roomId: Long, isPublic: Boolean? = null, roomPassword: String? = null): Result<Unit>

    // Room Discovery
    suspend fun getPopularRooms(page: Int, limit: Int): Result<Pair<List<RoomCardData>, Boolean>>
    suspend fun getRecentRooms(page: Int, limit: Int): Result<Pair<List<RoomCardData>, Boolean>>
    suspend fun getRoomDetail(roomId: Long): Result<RoomDetail>

    // Password Reset
    suspend fun resetRoomPassword(roomId: Long): Result<String>
}
