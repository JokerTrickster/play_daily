package response

type ResAuth struct {
	AccessToken           string `json:"access_token"`
	AccessTokenExpiredAt  int64  `json:"access_token_expired_at"`
	RefreshToken          string `json:"refresh_token"`
	RefreshTokenExpiredAt int64  `json:"refresh_token_expired_at"`
	UserID                uint   `json:"user_id"`
	AccountID             string `json:"account_id"`
	Nickname              string `json:"nickname"`
	DefaultRoomID         *uint  `json:"default_room_id,omitempty"`
}
