package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

data class UpdateRoomPrivacyResponseDto(
    @SerializedName("room_id")
    val roomId: Long,

    @SerializedName("is_public")
    val isPublic: Boolean,

    @SerializedName("room_password")
    val roomPassword: String?,

    @SerializedName("updated_at")
    val updatedAt: String
)
