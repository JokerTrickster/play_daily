package com.dailymemo.domain.usecases.comment

import com.dailymemo.domain.models.Comment
import com.dailymemo.domain.repositories.CommentRepository
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(memoId: Long): Result<List<Comment>> {
        return commentRepository.getComments(memoId)
    }
}
