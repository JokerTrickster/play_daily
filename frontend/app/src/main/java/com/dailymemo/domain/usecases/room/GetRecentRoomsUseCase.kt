package com.dailymemo.domain.usecases.room

import com.dailymemo.domain.repositories.RoomRepository
import com.dailymemo.presentation.room.RoomCardData
import javax.inject.Inject

/**
 * Use case for fetching recently created rooms
 */
class GetRecentRoomsUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(page: Int, limit: Int = 20): Result<Pair<List<RoomCardData>, Boolean>> {
        return roomRepository.getRecentRooms(page, limit)
    }
}
