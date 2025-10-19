package response

import "time"

type ResMemo struct {
	ID           uint      `json:"id"`
	UserID       uint      `json:"user_id"`
	Title        string    `json:"title"`
	Content      string    `json:"content"`
	ImageURL     string    `json:"image_url"`
	Rating       uint8     `json:"rating"`
	IsPinned     bool      `json:"is_pinned"`
	Latitude     *float64  `json:"latitude"`
	Longitude    *float64  `json:"longitude"`
	LocationName *string   `json:"location_name"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

type ResMemoList struct {
	Memos []ResMemo `json:"memos"`
	Total int64     `json:"total"`
}
