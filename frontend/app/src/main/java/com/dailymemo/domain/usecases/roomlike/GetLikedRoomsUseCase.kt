package com.dailymemo.domain.usecases.roomlike

import com.dailymemo.domain.models.LikedRoom
import com.dailymemo.domain.repositories.RoomLikeRepository
import javax.inject.Inject

class GetLikedRoomsUseCase @Inject constructor(
    private val roomLikeRepository: RoomLikeRepository
) {
    suspend operator fun invoke(): Result<List<LikedRoom>> {
        return roomLikeRepository.getLikedRooms()
    }
}
