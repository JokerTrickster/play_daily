package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.RoomMember
import com.dailymemo.domain.models.RoomPermission

interface RoomRepository {
    suspend fun joinRoom(roomId: Long, roomPassword: String): Result<Unit>
    suspend fun kickUser(roomId: Long, targetUserId: Long): Result<Unit>
    suspend fun getRoomMembers(roomId: Long): Result<List<RoomMember>>
    suspend fun updateMemberPermission(roomId: Long, userId: Long, permission: RoomPermission): Result<Unit>
    suspend fun updateRoomSettings(roomId: Long, isPublic: Boolean? = null, roomPassword: String? = null): Result<Unit>
}
