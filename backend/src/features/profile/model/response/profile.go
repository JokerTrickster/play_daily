package response

// ResProfile 프로필 조회 응답
type ResProfile struct {
	UserID          uint    `json:"user_id"`
	AccountID       string  `json:"account_id"`
	Nickname        string  `json:"nickname"`
	ProfileImageURL *string `json:"profile_image_url"`
	DefaultRoomID   *uint   `json:"default_room_id"`
}
