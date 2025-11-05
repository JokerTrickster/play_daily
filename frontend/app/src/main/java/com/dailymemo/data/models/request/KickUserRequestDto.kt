package com.dailymemo.data.models.request

import com.google.gson.annotations.SerializedName

data class KickUserRequestDto(
    @SerializedName("room_id")
    val roomId: Long,
    @SerializedName("target_user_id")
    val targetUserId: Long
)
