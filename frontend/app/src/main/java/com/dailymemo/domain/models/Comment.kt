package com.dailymemo.domain.models

import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val memoId: Long,
    val userId: Long,
    val userName: String,
    val content: String,
    val rating: Int = 0, // 댓글 작성자의 평점 (0-5)
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
