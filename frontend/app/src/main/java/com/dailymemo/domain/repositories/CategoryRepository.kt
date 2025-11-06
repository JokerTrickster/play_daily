package com.dailymemo.domain.repositories

import com.dailymemo.domain.models.MemoCategory

interface CategoryRepository {
    suspend fun getCategories(): Result<List<MemoCategory>>
    fun getCachedCategories(): List<MemoCategory>?
}
