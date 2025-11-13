package response

import (
	commentResponse "main/features/comment/model/response"
	"time"
)

// ResCategory 카테고리 응답 구조체
type ResCategory struct {
	ID           uint   `json:"id"`
	Name         string `json:"name"`
	Sentiment    string `json:"sentiment"`
	Color        string `json:"color"`
	DisplayOrder int    `json:"display_order"`
}

type ResMemo struct {
	ID              uint                         `json:"id"`
	UserID          uint                         `json:"user_id"`
	Title           string                       `json:"title"`
	Content         string                       `json:"content"` // Deprecated - will be empty after migration
	CreationMode    string                       `json:"creation_mode"` // NEW: map or list
	Categories      []ResCategory                `json:"categories"` // NEW: 선택된 카테고리 배열
	ImageURL        string                       `json:"image_url"`
	Rating          float32                      `json:"rating"` // Changed from uint8 to float32 for decimal support
	IsPinned        bool                         `json:"is_pinned"`
	LikesCount      uint                         `json:"likes_count"`
	IsLiked         bool                         `json:"is_liked"`
	Latitude        *float64                     `json:"latitude"`
	Longitude       *float64                     `json:"longitude"`
	LocationName    *string                      `json:"location_name"`
	Category        *string                      `json:"category"` // Deprecated
	// Wishlist fields (Issue #19)
	IsWishlist      bool                         `json:"is_wishlist"`
	BusinessName    *string                      `json:"business_name,omitempty"`
	BusinessPhone   *string                      `json:"business_phone,omitempty"`
	BusinessAddress *string                      `json:"business_address,omitempty"`
	NaverPlaceURL   *string                      `json:"naver_place_url,omitempty"`
	Comments        []commentResponse.ResComment `json:"comments,omitempty"`
	CreatedAt       time.Time                    `json:"created_at"`
	UpdatedAt       time.Time                    `json:"updated_at"`
}

type ResMemoList struct {
	Memos []ResMemo `json:"memos"`
	Total int64     `json:"total"`
	Page  int       `json:"page"`
	Limit int       `json:"limit"`
}
