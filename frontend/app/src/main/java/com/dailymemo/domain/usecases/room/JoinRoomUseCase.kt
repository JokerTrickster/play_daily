package com.dailymemo.domain.usecases.room

import com.dailymemo.domain.repositories.RoomRepository
import javax.inject.Inject

class JoinRoomUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(roomId: Long, roomPassword: String): Result<Unit> {
        return roomRepository.joinRoom(roomId, roomPassword)
    }
}
