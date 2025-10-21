package com.dailymemo.data.repositories

import com.dailymemo.data.datasources.remote.api.CommentApiService
import com.dailymemo.data.models.request.CreateCommentRequestDto
import com.dailymemo.domain.models.Comment
import com.dailymemo.domain.repositories.CommentRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentApiService: CommentApiService
) : CommentRepository {

    override suspend fun getComments(memoId: Long): Result<List<Comment>> {
        return try {
            val response = commentApiService.getComments(memoId)
            if (response.isSuccessful) {
                val commentList = response.body()?.comments ?: emptyList()
                val comments = commentList.map { dto ->
                    Comment(
                        id = dto.id,
                        memoId = dto.memo_id,
                        userId = dto.user_id,
                        userName = dto.user_name,
                        content = dto.content,
                        rating = dto.rating,
                        createdAt = parseDateTime(dto.created_at),
                        updatedAt = parseDateTime(dto.updated_at)
                    )
                }
                Result.success(comments)
            } else {
                Result.failure(Exception("Failed to get comments: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createComment(memoId: Long, content: String): Result<Comment> {
        return try {
            val request = CreateCommentRequestDto(content = content, rating = 0)
            val response = commentApiService.createComment(memoId, request)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    val comment = Comment(
                        id = dto.id,
                        memoId = dto.memo_id,
                        userId = dto.user_id,
                        userName = dto.user_name,
                        content = dto.content,
                        rating = dto.rating,
                        createdAt = parseDateTime(dto.created_at),
                        updatedAt = parseDateTime(dto.updated_at)
                    )
                    Result.success(comment)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Failed to create comment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(commentId: Long): Result<Unit> {
        return try {
            val response = commentApiService.deleteComment(commentId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete comment: ${response.code()}"))
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
