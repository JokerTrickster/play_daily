package com.dailymemo.domain.models

import java.time.LocalDateTime

data class Memo(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val imageUrls: List<String> = emptyList(), // 여러 이미지 지원
    val rating: Int,
    val isPinned: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val category: PlaceCategory = PlaceCategory.OTHER,
    val isWishlist: Boolean = false, // 위시리스트 여부 (true=가고싶은곳, false=방문한곳)
    val businessName: String? = null, // 장소/가게명
    val businessPhone: String? = null, // 전화번호
    val businessAddress: String? = null, // 주소
    val comments: List<Comment> = emptyList(), // 댓글 목록
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
