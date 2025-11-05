package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.RoomApiService
import com.dailymemo.data.models.request.JoinRoomRequestDto
import com.dailymemo.data.models.request.KickUserRequestDto
import com.dailymemo.domain.error.DomainError
import com.dailymemo.domain.models.RoomMember
import com.dailymemo.domain.models.RoomPermission
import com.dailymemo.domain.repositories.RoomRepository
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val roomApiService: RoomApiService
) : RoomRepository {

    override suspend fun joinRoom(roomId: Long, roomPassword: String): Result<Unit> {
        return try {
            val request = JoinRoomRequestDto(
                roomId = roomId,
                roomPassword = roomPassword
            )
            val response = roomApiService.joinRoom(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val error = when (response.code()) {
                    404 -> DomainError.RoomNotFound
                    401 -> DomainError.InvalidRoomPassword
                    else -> DomainError.RoomJoinFailed
                }
                Result.failure(error)
            }
        } catch (e: Exception) {
            val error = when (e) {
                is java.net.UnknownHostException -> DomainError.NoConnection
                is java.net.SocketTimeoutException -> DomainError.Timeout
                is java.io.IOException -> DomainError.NetworkError(e)
                else -> DomainError.UnknownError
            }
            Result.failure(error)
        }
    }

    override suspend fun kickUser(roomId: Long, targetUserId: Long): Result<Unit> {
        return try {
            val request = KickUserRequestDto(
                roomId = roomId,
                targetUserId = targetUserId
            )
            val response = roomApiService.kickUser(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val error = when (response.code()) {
                    403 -> DomainError.Forbidden
                    400 -> DomainError.BadRequest
                    404 -> DomainError.RoomNotFound
                    else -> DomainError.UnknownError
                }
                Result.failure(error)
            }
        } catch (e: Exception) {
            val error = when (e) {
                is java.net.UnknownHostException -> DomainError.NoConnection
                is java.net.SocketTimeoutException -> DomainError.Timeout
                is java.io.IOException -> DomainError.NetworkError(e)
                else -> DomainError.UnknownError
            }
            Result.failure(error)
        }
    }

    override suspend fun getRoomMembers(roomId: Long): Result<List<RoomMember>> {
        return try {
            val response = roomApiService.getRoomMembers(roomId)
            if (response.isSuccessful && response.body() != null) {
                val members = response.body()!!.members.map { dto ->
                    RoomMember(
                        userId = dto.userId,
                        userName = dto.userName,
                        email = dto.email,
                        permission = RoomPermission.fromString(dto.permission),
                        joinedAt = dto.joinedAt
                    )
                }
                Result.success(members)
            } else {
                val error = when (response.code()) {
                    403 -> DomainError.Forbidden
                    404 -> DomainError.RoomNotFound
                    else -> DomainError.UnknownError
                }
                Result.failure(error)
            }
        } catch (e: Exception) {
            val error = when (e) {
                is java.net.UnknownHostException -> DomainError.NoConnection
                is java.net.SocketTimeoutException -> DomainError.Timeout
                is java.io.IOException -> DomainError.NetworkError(e)
                else -> DomainError.UnknownError
            }
            Result.failure(error)
        }
    }
}
