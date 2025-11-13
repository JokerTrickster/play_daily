package com.dailymemo.data.remote.dto

import com.dailymemo.domain.models.Profile
import com.google.gson.annotations.SerializedName

/**
 * 프로필 응답 DTO
 * @property userId 사용자 ID
 * @property accountId 계정 ID
 * @property nickname 닉네임
 * @property bio 자기소개
 * @property profileImageUrl 프로필 이미지 URL
 * @property defaultRoomId 기본 방 ID
 * @property roomPassword 방 비밀번호 (4자리)
 */
data class ProfileResponse(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("account_id")
    val accountId: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,

    @SerializedName("default_room_id")
    val defaultRoomId: Int? = null,

    @SerializedName("room_password")
    val roomPassword: String,

    @SerializedName("received_likes_count")
    val receivedLikesCount: Int = 0
) {
    /**
     * DTO를 도메인 모델로 변환
     */
    fun toDomain(): Profile {
        return Profile(
            userId = userId,
            accountId = accountId,
            nickname = nickname,
            bio = bio,
            profileImageUrl = profileImageUrl,
            defaultRoomId = defaultRoomId,
            roomPassword = roomPassword,
            receivedLikesCount = receivedLikesCount
        )
    }
}
