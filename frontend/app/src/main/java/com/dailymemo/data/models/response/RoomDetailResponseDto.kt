package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

data class RoomDetailResponseDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("room_code")
    val roomCode: String,

    @SerializedName("is_public")
    val isPublic: Boolean,

    @SerializedName("likes_count")
    val likesCount: Long,

    @SerializedName("owner_id")
    val ownerId: Long,

    @SerializedName("owner_name")
    val ownerName: String,

    @SerializedName("owner_bio")
    val ownerBio: String?,

    @SerializedName("created_at")
    val createdAt: String
)
