package request

// ReqUpdateProfile 프로필 업데이트 요청
type ReqUpdateProfile struct {
	CurrentPassword string  `json:"current_password" binding:"required"`
	Nickname        *string `json:"nickname"`
	NewPassword     *string `json:"new_password"`
	ProfileImageURL *string `json:"profile_image_url"`
}
