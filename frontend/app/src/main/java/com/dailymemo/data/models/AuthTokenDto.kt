package com.dailymemo.data.models

data class AuthTokenDto(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val username: String,
    val memoSpaceId: String,
    val defaultRoomId: String? = null,
    val roomPassword: String? = null
)
