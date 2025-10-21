package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.repositories.MemoRepository
import javax.inject.Inject

class GetMemosUseCase @Inject constructor(
    private val repository: MemoRepository
) {
    suspend operator fun invoke(
        isWishlist: Boolean? = null
    ): Result<List<Memo>> {
        return repository.getMemos(isWishlist)
    }
}
