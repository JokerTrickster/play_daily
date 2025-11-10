package com.dailymemo.data.models.response

import com.google.gson.annotations.SerializedName

data class RoomMembersResponseDto(
    @SerializedName("room_id")
    val roomId: Long,
    @SerializedName("members")
    val members: List<RoomMemberDto>
)

data class RoomMemberDto(
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("profile_image_url")
    val profileImageUrl: String?,
    @SerializedName("permission")
    val permission: String, // READ_ONLY, READ_WRITE, OWNER
    @SerializedName("joined_at")
    val joinedAt: Long
)
