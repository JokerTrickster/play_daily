package com.dailymemo.data.remote.dto

import com.dailymemo.domain.models.CategorySentiment
import com.dailymemo.domain.models.MemoCategory
import com.google.gson.annotations.SerializedName

/**
 * 카테고리 DTO
 * @property id 카테고리 ID
 * @property name 카테고리 이름 (한국어)
 * @property sentiment 감정 분류 (positive/negative/neutral)
 * @property color 색상 코드 (Hex, e.g., "#10B981")
 * @property displayOrder 표시 순서
 */
data class CategoryDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("sentiment")
    val sentiment: String,

    @SerializedName("color")
    val color: String,

    @SerializedName("display_order")
    val displayOrder: Int
) {
    /**
     * DTO를 도메인 모델로 변환
     */
    fun toDomain(): MemoCategory {
        return MemoCategory(
            id = id,
            name = name,
            sentiment = CategorySentiment.fromString(sentiment),
            color = color,
            displayOrder = displayOrder
        )
    }
}
