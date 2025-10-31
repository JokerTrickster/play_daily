package com.dailymemo.domain.usecases.roomlike

import com.dailymemo.domain.models.RoomLikeStatus
import com.dailymemo.domain.repositories.RoomLikeRepository
import javax.inject.Inject

class GetRoomLikeStatusUseCase @Inject constructor(
    private val roomLikeRepository: RoomLikeRepository
) {
    suspend operator fun invoke(roomId: Long): Result<RoomLikeStatus> {
        return roomLikeRepository.getRoomLikeStatus(roomId)
    }
}
