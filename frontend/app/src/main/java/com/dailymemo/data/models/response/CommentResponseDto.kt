package com.dailymemo.data.models.response

data class CommentResponseDto(
    val id: Long,
    val memo_id: Long,
    val user_id: Long,
    val user_name: String,
    val content: String,
    val rating: Int,
    val created_at: String,
    val updated_at: String
)

data class CommentListResponseDto(
    val comments: List<CommentResponseDto>,
    val total: Long
)
