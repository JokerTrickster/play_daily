package _interface

import (
	"context"
	"main/common/db/mysql"
	"time"
)

type IReissueAuthRepository interface {
	GetTokenByRefreshToken(ctx context.Context, refreshToken string) (*mysql.Token, error)
	RevokeToken(ctx context.Context, refreshToken string) error
	StoreToken(ctx context.Context, userID uint, accessToken, refreshToken string, accessTokenExpiresAt, refreshTokenExpiresAt time.Time) error
}
