package com.dailymemo.data.remote.dto

import com.dailymemo.domain.models.MemoCategory
import com.google.gson.annotations.SerializedName

/**
 * 카테고리 목록 응답 DTO
 * @property categories 카테고리 목록
 */
data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<CategoryDto>
) {
    /**
     * DTO를 도메인 모델 리스트로 변환
     */
    fun toDomain(): List<MemoCategory> {
        return categories.map { it.toDomain() }
    }
}
