package com.dailymemo.domain.usecases.room

import com.dailymemo.domain.models.RoomMember
import com.dailymemo.domain.repositories.RoomRepository
import javax.inject.Inject

class GetRoomMembersUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(roomId: Long): Result<List<RoomMember>> {
        return roomRepository.getRoomMembers(roomId)
    }
}
