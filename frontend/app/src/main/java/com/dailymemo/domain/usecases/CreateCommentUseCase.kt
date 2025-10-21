package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.Comment
import com.dailymemo.domain.repositories.CommentRepository
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(memoId: Long, content: String): Result<Comment> {
        return commentRepository.createComment(memoId, content)
    }
}
