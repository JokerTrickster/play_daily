package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.RoomApiService
import com.dailymemo.data.models.request.JoinRoomRequestDto
import com.dailymemo.data.models.request.KickUserRequestDto
import com.dailymemo.domain.error.DomainError
import com.dailymemo.domain.models.MemoCategory
import com.dailymemo.domain.models.CategorySentiment
import com.dailymemo.domain.models.RoomMember
import com.dailymemo.domain.models.RoomPermission
import com.dailymemo.domain.repositories.RoomRepository
import com.dailymemo.presentation.room.RoomCardData
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
                        profileImageUrl = dto.profileImageUrl,
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

    override suspend fun updateRoomSettings(
        roomId: Long,
        isPublic: Boolean?,
        roomPassword: String?
    ): Result<Unit> {
        return try {
            val request = mutableMapOf<String, Any>("room_id" to roomId)
            isPublic?.let { request["is_public"] = it }
            roomPassword?.let { request["room_password"] = it }

            val response = roomApiService.updateRoomSettings(request)
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

    override suspend fun getPopularRooms(page: Int, limit: Int): Result<Pair<List<RoomCardData>, Boolean>> {
        return try {
            val response = roomApiService.getPopularRooms(page, limit)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val rooms = dto.rooms.map { roomDto ->
                    val publicStatus = if (roomDto.isPublic) "공개방" else "비공개방"
                    val likesInfo = if (roomDto.likesCount > 0) " · ❤️ ${roomDto.likesCount}" else ""
                    RoomCardData(
                        id = roomDto.id.toString(),
                        title = roomDto.name,
                        description = "$publicStatus$likesInfo",
                        participantCount = 0, // Backend doesn't return participant count yet
                        categories = emptyList(), // Backend doesn't return categories yet
                        thumbnailUrl = null,
                        ownerName = "User ${roomDto.ownerId}",
                        isPublic = roomDto.isPublic
                    )
                }
                Result.success(Pair(rooms, dto.hasNext))
            } else {
                val error = when (response.code()) {
                    400 -> DomainError.BadRequest
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

    override suspend fun getRecentRooms(page: Int, limit: Int): Result<Pair<List<RoomCardData>, Boolean>> {
        return try {
            val response = roomApiService.getRecentRooms(page, limit)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val rooms = dto.rooms.map { roomDto ->
                    val publicStatus = if (roomDto.isPublic) "공개방" else "비공개방"
                    val likesInfo = if (roomDto.likesCount > 0) " · ❤️ ${roomDto.likesCount}" else ""
                    RoomCardData(
                        id = roomDto.id.toString(),
                        title = roomDto.name,
                        description = "$publicStatus$likesInfo",
                        participantCount = 0, // Backend doesn't return participant count yet
                        categories = emptyList(), // Backend doesn't return categories yet
                        thumbnailUrl = null,
                        ownerName = "User ${roomDto.ownerId}",
                        isPublic = roomDto.isPublic
                    )
                }
                Result.success(Pair(rooms, dto.hasNext))
            } else {
                val error = when (response.code()) {
                    400 -> DomainError.BadRequest
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

    override suspend fun resetRoomPassword(roomId: Long): Result<String> {
        return try {
            val response = roomApiService.resetRoomPassword(roomId)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                if (dto.success) {
                    Result.success(dto.newPassword)
                } else {
                    Result.failure(DomainError.UnknownError)
                }
            } else {
                val error = when (response.code()) {
                    401 -> DomainError.Unauthorized
                    403 -> DomainError.Forbidden
                    404 -> DomainError.RoomNotFound
                    429 -> DomainError.RateLimitExceeded
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
