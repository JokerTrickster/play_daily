package com.dailymemo.domain.usecases

import com.dailymemo.domain.repositories.MemoRepository
import javax.inject.Inject

class ToggleMemoLikeUseCase @Inject constructor(
    private val repository: MemoRepository
) {
    suspend operator fun invoke(memoId: Long): Result<Boolean> {
        return repository.toggleMemoLike(memoId)
    }
}
