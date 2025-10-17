package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.MemoApiService
import com.dailymemo.data.models.request.CreateMemoRequestDto
import com.dailymemo.data.models.request.UpdateMemoRequestDto
import com.dailymemo.data.models.response.MemoDto
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.repositories.MemoRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepositoryImpl @Inject constructor(
    private val memoApiService: MemoApiService
) : MemoRepository {

    override suspend fun getMemos(): Result<List<Memo>> {
        return try {
            val response = memoApiService.getMemos()
            if (response.isSuccessful && response.body() != null) {
                val memos = response.body()!!.map { it.toDomain() }
                Result.success(memos)
            } else {
                Result.failure(Exception("메모 목록 조회에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun getMemo(id: Long): Result<Memo> {
        return try {
            val response = memoApiService.getMemo(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("메모 조회에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun createMemo(
        title: String,
        content: String,
        imageUrl: String?,
        rating: Int,
        isPinned: Boolean
    ): Result<Memo> {
        return try {
            val request = CreateMemoRequestDto(
                title = title,
                content = content,
                imageUrl = imageUrl,
                rating = rating,
                isPinned = isPinned
            )
            val response = memoApiService.createMemo(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("메모 생성에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun updateMemo(
        id: Long,
        title: String,
        content: String,
        imageUrl: String?,
        rating: Int,
        isPinned: Boolean
    ): Result<Memo> {
        return try {
            val request = UpdateMemoRequestDto(
                title = title,
                content = content,
                imageUrl = imageUrl,
                rating = rating,
                isPinned = isPinned
            )
            val response = memoApiService.updateMemo(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("메모 수정에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    override suspend fun deleteMemo(id: Long): Result<Unit> {
        return try {
            val response = memoApiService.deleteMemo(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("메모 삭제에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류: ${e.message}"))
        }
    }

    private fun MemoDto.toDomain(): Memo {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return Memo(
            id = id,
            userId = userId,
            title = title,
            content = content,
            imageUrl = imageUrl,
            rating = rating,
            isPinned = isPinned,
            createdAt = LocalDateTime.parse(createdAt, formatter),
            updatedAt = LocalDateTime.parse(updatedAt, formatter)
        )
    }
}
