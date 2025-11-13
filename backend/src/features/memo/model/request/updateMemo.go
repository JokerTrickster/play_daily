package request

import "mime/multipart"

type ReqUpdateMemo struct {
	Title           string                `json:"title"`
	Content         string                `json:"content"`
	ImageURL        string                `json:"image_url"`
	ImageFile       multipart.File        `json:"-"` // S3 업로드용 파일
	ImageHeader     *multipart.FileHeader `json:"-"` // 파일 메타데이터
	Rating          float32               `json:"rating"` // float로 받아서 uint8로 변환
	IsPinned        bool                  `json:"is_pinned"`
	Latitude        *float64              `json:"latitude"`
	Longitude       *float64              `json:"longitude"`
	LocationName    *string               `json:"location_name"`
	IsWishlist      bool                  `json:"is_wishlist"`
	BusinessName    *string               `json:"business_name"`
	BusinessPhone   *string               `json:"business_phone"`
	BusinessAddress *string               `json:"business_address"`
	CategoryIds     []int                 `json:"category_ids"`
}
