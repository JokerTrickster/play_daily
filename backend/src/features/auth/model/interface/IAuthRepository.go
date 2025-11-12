package _interface

import (
	"context"
	"main/common/db/mysql"
	"time"
)

type ISignInAuthRepository interface {
	CheckPassword(ctx context.Context, id, password string) (*mysql.User, error)
	StoreToken(ctx context.Context, userID uint, accessToken, refreshToken string, accessTokenExpiresAt, refreshTokenExpiresAt time.Time) error
}

type ISignUpAuthRepository interface {
	CheckAccountIDDuplicate(ctx context.Context, accountID string) error
	CreateUser(ctx context.Context, userDTO *mysql.User) (*mysql.User, error)
	StoreToken(ctx context.Context, userID uint, accessToken, refreshToken string, accessTokenExpiresAt, refreshTokenExpiresAt time.Time) error
}
