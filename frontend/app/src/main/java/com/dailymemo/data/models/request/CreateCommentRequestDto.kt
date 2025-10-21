package com.dailymemo.data.models.request

data class CreateCommentRequestDto(
    val content: String,
    val rating: Int = 0
)
