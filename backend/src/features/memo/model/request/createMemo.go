package request

import "mime/multipart"

type ReqCreateMemo struct {
	RoomID          uint                  `json:"room_id"` // Handler에서 기본값 설정
	Title           string                `json:"title" binding:"required"`
	Content         string                `json:"content"` // Deprecated - will be removed after migration
	CreationMode    string                `json:"creation_mode" binding:"required,oneof=map list"` // NEW: 생성 방식
	CategoryIDs     []uint                `json:"category_ids" binding:"required,min=1"` // NEW: 선택된 카테고리 ID 배열
	ImageURL        string                `json:"image_url"`
	ImageFile       multipart.File        `json:"-"` // S3 업로드용 파일
	ImageHeader     *multipart.FileHeader `json:"-"` // 파일 메타데이터
	Rating          uint8                 `json:"rating"`
	IsPinned        bool                  `json:"is_pinned"`
	Latitude        *float64              `json:"latitude"`
	Longitude       *float64              `json:"longitude"`
	LocationName    *string               `json:"location_name"`
	Category        *string               `json:"category"` // Deprecated
	IsWishlist      bool                  `json:"is_wishlist"`
	BusinessName    *string               `json:"business_name"`
	BusinessPhone   *string               `json:"business_phone"`
	BusinessAddress *string               `json:"business_address"`
	NaverPlaceURL   *string               `json:"naver_place_url"`
}
