package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("access_token_expired_at") val accessTokenExpiredAt: Long,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("refresh_token_expired_at") val refreshTokenExpiredAt: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("account_id") val accountId: String,
    @SerializedName("nickname") val nickname: String
)
