package response

type ResReissue struct {
	AccessToken           string `json:"access_token"`
	RefreshToken          string `json:"refresh_token"`
	AccessTokenExpiredAt  int64  `json:"access_token_expired_at"`
	RefreshTokenExpiredAt int64  `json:"refresh_token_expired_at"`
}
