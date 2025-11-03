package com.dailymemo.domain.repositories

interface RoomRepository {
    suspend fun joinRoom(roomId: Long, roomPassword: String): Result<Unit>
}
