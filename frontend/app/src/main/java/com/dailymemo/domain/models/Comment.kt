package com.dailymemo.domain.models

import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val memoId: Long,
    val userId: Long,
    val userName: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
