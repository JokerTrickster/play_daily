package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.RoomApiService
import com.dailymemo.data.models.request.JoinRoomRequestDto
import com.dailymemo.data.models.request.KickUserRequestDto
import com.dailymemo.data.models.request.UpdateRoomPrivacyRequestDto
import com.dailymemo.domain.error.DomainError
import com.dailymemo.domain.models.PublicRoom
import com.dailymemo.domain.models.PublicRoomsResult
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

    override suspend fun updateMemberPermission(
        roomId: Long,
        userId: Long,
        permission: RoomPermission
    ): Result<Unit> {
        return try {
            val permissionString = when (permission) {
                RoomPermission.OWNER -> "OWNER"
                RoomPermission.READ_WRITE -> "READ_WRITE"
                RoomPermission.READ_ONLY -> "READ_ONLY"
            }
            val request = mapOf(
                "room_id" to roomId,
                "user_id" to userId,
                "permission" to permissionString
            )
            val response = roomApiService.updateMemberPermission(request)
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

    override suspend fun updateRoomPrivacy(roomId: Long, isPublic: Boolean): Result<Pair<Boolean, String?>> {
        return try {
            val request = UpdateRoomPrivacyRequestDto(isPublic = isPublic)
            val response = roomApiService.updateRoomPrivacy(roomId, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(Pair(body.isPublic, body.roomPassword))
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

    override suspend fun getPublicRooms(page: Int, limit: Int): Result<PublicRoomsResult> {
        return try {
            val response = roomApiService.getPublicRooms(page, limit)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val rooms = body.rooms.map { dto ->
                    PublicRoom(
                        roomId = dto.roomId,
                        roomName = dto.roomName,
                        roomCode = dto.roomCode,
                        ownerName = dto.ownerName,
                        likesCount = dto.likesCount,
                        membersCount = dto.membersCount
                    )
                }
                Result.success(
                    PublicRoomsResult(
                        rooms = rooms,
                        total = body.total,
                        page = body.page,
                        limit = body.limit,
                        hasMore = body.hasMore
                    )
                )
            } else {
                val error = when (response.code()) {
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
