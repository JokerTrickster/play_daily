package com.dailymemo.domain.models

import java.time.LocalDateTime

data class Memo(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String, // Deprecated - will be removed
    val creationMode: CreationMode = CreationMode.LIST, // NEW: 생성 방식 (map/list)
    val categories: List<MemoCategory> = emptyList(), // NEW: 선택된 카테고리 목록
    val imageUrl: String?,
    val imageUrls: List<String> = emptyList(), // 여러 이미지 지원
    val rating: Float, // 0.0 ~ 5.0 (0.5 단위)
    val isPinned: Boolean,
    val likesCount: Int = 0, // 좋아요 개수
    val isLiked: Boolean = false, // 현재 사용자의 좋아요 여부
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val category: PlaceCategory = PlaceCategory.OTHER, // Deprecated
    val isWishlist: Boolean = false, // 위시리스트 여부 (true=가고싶은곳, false=방문한곳)
    val businessName: String? = null, // 장소/가게명
    val businessPhone: String? = null, // 전화번호
    val businessAddress: String? = null, // 주소
    val naverPlaceUrl: String? = null, // 네이버 플레이스 URL
    val comments: List<Comment> = emptyList(), // 댓글 목록
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class MemoListResult(
    val memos: List<Memo>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)
