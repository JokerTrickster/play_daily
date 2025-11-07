package com.dailymemo.data.models.request

import com.google.gson.annotations.SerializedName

data class UpdateRoomPrivacyRequestDto(
    @SerializedName("is_public")
    val isPublic: Boolean
)
