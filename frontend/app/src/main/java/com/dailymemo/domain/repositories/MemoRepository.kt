package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.Memo

interface MemoRepository {
    suspend fun getMemos(): Result<List<Memo>>
    suspend fun getMemo(id: Long): Result<Memo>
    suspend fun createMemo(
        title: String,
        content: String,
        imageUrl: String? = null,
        rating: Int = 0,
        isPinned: Boolean = false
    ): Result<Memo>
    suspend fun updateMemo(
        id: Long,
        title: String,
        content: String,
        imageUrl: String? = null,
        rating: Int = 0,
        isPinned: Boolean = false
    ): Result<Memo>
    suspend fun deleteMemo(id: Long): Result<Unit>
}
