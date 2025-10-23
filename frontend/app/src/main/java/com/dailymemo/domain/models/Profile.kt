package com.dailymemo.domain.models

/**
 * 프로필 도메인 모델
 * @property userId 사용자 ID
 * @property accountId 계정 ID
 * @property nickname 닉네임
 * @property profileImageUrl 프로필 이미지 URL (nullable)
 * @property defaultRoomId 기본 방 ID (nullable)
 */
data class Profile(
    val userId: Int,
    val accountId: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val defaultRoomId: Int? = null
)
