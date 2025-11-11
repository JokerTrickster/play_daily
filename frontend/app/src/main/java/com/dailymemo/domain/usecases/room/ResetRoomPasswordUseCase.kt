package com.dailymemo.domain.usecases.room

import com.dailymemo.domain.repositories.RoomRepository
import javax.inject.Inject

/**
 * Use case for resetting room password
 * Returns the new auto-generated password
 */
class ResetRoomPasswordUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(roomId: Long): Result<String> {
        return roomRepository.resetRoomPassword(roomId)
    }
}
