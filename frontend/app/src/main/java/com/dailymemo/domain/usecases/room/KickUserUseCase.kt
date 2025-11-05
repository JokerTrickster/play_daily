package com.dailymemo.domain.usecases.room

import com.dailymemo.domain.repositories.RoomRepository
import javax.inject.Inject

class KickUserUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(roomId: Long, targetUserId: Long): Result<Unit> {
        return roomRepository.kickUser(roomId, targetUserId)
    }
}
