package com.dailymemo.domain.usecases

import com.dailymemo.data.datasources.local.AuthLocalDataSource
import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.models.MemoListResult
import com.dailymemo.domain.repositories.MemoRepository
import javax.inject.Inject

class GetMemosUseCase @Inject constructor(
    private val repository: MemoRepository,
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend operator fun invoke(
        isWishlist: Boolean? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<MemoListResult> {
        val currentRoomId = authLocalDataSource.getCurrentRoomId()
        return repository.getMemos(isWishlist, currentRoomId, page, limit)
    }
}
