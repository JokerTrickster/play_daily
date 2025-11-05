package com.dailymemo.domain.usecases.room

import com.dailymemo.domain.models.RoomPermission
import com.dailymemo.domain.repositories.RoomRepository
import javax.inject.Inject

class UpdateMemberPermissionUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(
        roomId: Long,
        userId: Long,
        permission: RoomPermission
    ): Result<Unit> {
        return roomRepository.updateMemberPermission(roomId, userId, permission)
    }
}
