package com.dailymemo.domain.usecases.room

import com.dailymemo.domain.repositories.RoomRepository
import com.dailymemo.presentation.room.RoomDetail
import javax.inject.Inject

/**
 * Use case for fetching room detail information including owner bio
 */
class GetRoomDetailUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(roomId: Long): Result<RoomDetail> {
        return roomRepository.getRoomDetail(roomId)
    }
}
