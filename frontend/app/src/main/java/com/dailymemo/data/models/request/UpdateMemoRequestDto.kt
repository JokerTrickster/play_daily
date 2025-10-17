package com.dailymemo.data.models.request

import com.google.gson.annotations.SerializedName

data class UpdateMemoRequestDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("rating")
    val rating: Int = 0,
    @SerializedName("is_pinned")
    val isPinned: Boolean = false
)
