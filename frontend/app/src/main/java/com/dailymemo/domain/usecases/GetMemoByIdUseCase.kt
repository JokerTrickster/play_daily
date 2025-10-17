package com.dailymemo.domain.usecases

import com.dailymemo.domain.models.Memo
import com.dailymemo.domain.repositories.MemoRepository
import javax.inject.Inject

class GetMemoByIdUseCase @Inject constructor(
    private val repository: MemoRepository
) {
    suspend operator fun invoke(id: Long): Result<Memo> {
        return repository.getMemo(id)
    }
}
