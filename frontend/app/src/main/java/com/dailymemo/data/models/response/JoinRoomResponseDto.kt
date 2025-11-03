package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

data class JoinRoomResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("owner_id")
    val ownerId: Long,
    @SerializedName("room_password")
    val roomPassword: String
)
