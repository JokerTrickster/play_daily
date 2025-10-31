package com.dailymemo.domain.usecases.roomlike

import com.dailymemo.domain.repositories.RoomLikeRepository
import javax.inject.Inject

class UnlikeRoomUseCase @Inject constructor(
    private val roomLikeRepository: RoomLikeRepository
) {
    suspend operator fun invoke(roomId: Long): Result<Unit> {
        return roomLikeRepository.unlikeRoom(roomId)
    }
}
