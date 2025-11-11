package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

/**
 * Reset password response DTO matching backend response
 * Backend: ResResetPassword in backend/src/features/room/model/response/resetPassword.go
 */
data class ResetPasswordResponseDto(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("new_password")
    val newPassword: String,

    @SerializedName("message")
    val message: String
)
