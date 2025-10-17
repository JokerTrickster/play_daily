package com.dailymemo.data.models.request

import com.google.gson.annotations.SerializedName

data class SignInRequestDto(
    @SerializedName("account_id")
    val accountId: String,
    @SerializedName("password")
    val password: String
)
