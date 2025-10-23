package com.dailymemo.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 프로필 업데이트 요청 DTO
 * @property currentPassword 현재 비밀번호 (필수)
 * @property nickname 변경할 닉네임 (선택)
 * @property newPassword 새 비밀번호 (선택)
 * @property profileImageUrl 프로필 이미지 URL (선택)
 */
data class UpdateProfileRequest(
    @SerializedName("current_password")
    val currentPassword: String,

    @SerializedName("nickname")
    val nickname: String? = null,

    @SerializedName("new_password")
    val newPassword: String? = null,

    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null
)
