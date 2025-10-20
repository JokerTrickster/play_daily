package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.Comment

interface CommentRepository {
    suspend fun getComments(memoId: Long): Result<List<Comment>>
    suspend fun createComment(memoId: Long, content: String): Result<Comment>
    suspend fun deleteComment(commentId: Long): Result<Unit>
}
