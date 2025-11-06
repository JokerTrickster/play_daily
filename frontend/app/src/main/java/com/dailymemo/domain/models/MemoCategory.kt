package com.dailymemo.domain.models

data class MemoCategory(
    val id: Int,
    val name: String,
    val sentiment: CategorySentiment,
    val color: String, // Hex color code (e.g., "#10B981")
    val displayOrder: Int
)
