package com.dailymemo.domain.usecases

import com.dailymemo.domain.repositories.MemoRepository
import javax.inject.Inject

class DeleteMemoUseCase @Inject constructor(
    private val repository: MemoRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return repository.deleteMemo(id)
    }
}
