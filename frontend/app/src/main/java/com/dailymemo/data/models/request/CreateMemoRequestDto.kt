package com.dailymemo.data.models.request

import com.google.gson.annotations.SerializedName

data class CreateMemoRequestDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("rating")
    val rating: Float = 0f,
    @SerializedName("is_pinned")
    val isPinned: Boolean = false,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("location_name")
    val locationName: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("is_wishlist")
    val isWishlist: Boolean = false,
    @SerializedName("business_name")
    val businessName: String? = null,
    @SerializedName("business_phone")
    val businessPhone: String? = null,
    @SerializedName("business_address")
    val businessAddress: String? = null
)
