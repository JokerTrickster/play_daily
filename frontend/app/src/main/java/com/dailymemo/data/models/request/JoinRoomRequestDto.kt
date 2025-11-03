package com.dailymemo.data.models.request

import com.google.gson.annotations.SerializedName

data class JoinRoomRequestDto(
    @SerializedName("room_id")
    val roomId: Long,
    @SerializedName("room_password")
    val roomPassword: String
)
