package com.dailymemo.domain.usecases.room

import com.dailymemo.domain.repositories.RoomRepository
import javax.inject.Inject

class UpdateRoomSettingsUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(
        roomId: Long,
        isPublic: Boolean? = null,
        roomPassword: String? = null
    ): Result<Unit> {
        return roomRepository.updateRoomSettings(roomId, isPublic, roomPassword)
    }
}
