package com.dailymemo.domain.usecases

import com.dailymemo.domain.repositories.CommentRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(commentId: Long): Result<Unit> {
        return commentRepository.deleteComment(commentId)
    }
}
