package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.RoomApiService
import com.dailymemo.data.models.request.JoinRoomRequestDto
import com.dailymemo.domain.error.DomainError
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
}
