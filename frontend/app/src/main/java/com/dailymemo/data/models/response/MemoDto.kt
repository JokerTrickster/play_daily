package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

data class MemoDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("rating")
    val rating: Float,
    @SerializedName("is_pinned")
    val isPinned: Boolean,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("location_name")
    val locationName: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("is_wishlist")
    val isWishlist: Boolean = false,
    @SerializedName("business_name")
    val businessName: String? = null,
    @SerializedName("business_phone")
    val businessPhone: String? = null,
    @SerializedName("business_address")
    val businessAddress: String? = null,
    @SerializedName("naver_place_url")
    val naverPlaceUrl: String? = null,
    @SerializedName("comments")
    val comments: List<CommentResponseDto>? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class MemoListDto(
    @SerializedName("memos")
    val memos: List<MemoDto>,
    @SerializedName("total")
    val total: Long
)
