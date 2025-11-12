package com.dailymemo.data.models.request

import com.google.gson.annotations.SerializedName

data class ReissueTokenRequest(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)
