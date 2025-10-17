package com.dailymemo.data.models.request

import com.google.gson.annotations.SerializedName

data class SignUpRequestDto(
    @SerializedName("account_id")
    val accountId: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("auth_code")
    val authCode: String,
    @SerializedName("nickname")
    val nickname: String
)
