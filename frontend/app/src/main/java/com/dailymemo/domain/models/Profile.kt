package com.dailymemo.domain.models

/**
 * 프로필 도메인 모델
 * @property userId 사용자 ID
 * @property accountId 계정 ID
 * @property nickname 닉네임
 * @property bio 자기소개 (nullable)
 * @property profileImageUrl 프로필 이미지 URL (nullable)
 * @property defaultRoomId 기본 방 ID (nullable)
 * @property roomPassword 방 비밀번호 (4자리)
 * @property receivedLikesCount 내 방이 받은 좋아요 개수
 */
data class Profile(
    val userId: Int,
    val accountId: String,
    val nickname: String,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val defaultRoomId: Int? = null,
    val roomPassword: String,
    val receivedLikesCount: Int = 0
)
