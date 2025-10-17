package com.dailymemo.domain.models

import java.time.LocalDateTime

data class Memo(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val rating: Int,
    val isPinned: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
