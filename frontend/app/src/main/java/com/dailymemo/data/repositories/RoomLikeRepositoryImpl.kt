package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.RoomLikeApiService
import com.dailymemo.domain.models.LikedRoom
import com.dailymemo.domain.models.RoomLikeStatus
import com.dailymemo.domain.repositories.RoomLikeRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RoomLikeRepositoryImpl @Inject constructor(
    private val roomLikeApiService: RoomLikeApiService
) : RoomLikeRepository {

    override suspend fun likeRoom(roomId: Long): Result<Unit> {
        return try {
            val response = roomLikeApiService.likeRoom(roomId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to like room: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikeRoom(roomId: Long): Result<Unit> {
        return try {
            val response = roomLikeApiService.unlikeRoom(roomId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to unlike room: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLikedRooms(): Result<List<LikedRoom>> {
        return try {
            val response = roomLikeApiService.getLikedRooms()
            if (response.isSuccessful) {
                val likedRoomList = response.body() ?: emptyList()
                val likedRooms = likedRoomList.map { dto ->
                    LikedRoom(
                        roomId = dto.room_id,
                        roomCode = dto.room_code,
                        roomName = dto.room_name,
                        ownerId = dto.owner_id,
                        likesCount = dto.likes_count,
                        likedAt = parseDateTime(dto.liked_at)
                    )
                }
                Result.success(likedRooms)
            } else {
                Result.failure(Exception("Failed to get liked rooms: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoomLikeStatus(roomId: Long): Result<RoomLikeStatus> {
        return try {
            val response = roomLikeApiService.getRoomLikeStatus(roomId)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    val status = RoomLikeStatus(
                        isLiked = dto.is_liked,
                        likesCount = dto.likes_count
                    )
                    Result.success(status)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to get room like status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseDateTime(dateTimeStr: String): LocalDateTime {
        return try {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            LocalDateTime.parse(dateTimeStr, formatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}
