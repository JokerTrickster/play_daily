package _interface

import (
	"context"
	"main/common/db/mysql"
)

type IGetProfileRepository interface {
	GetByUserID(ctx context.Context, userID uint) (*mysql.User, error)
}

type IUpdateProfileRepository interface {
	UpdateProfile(ctx context.Context, userID uint, currentPassword string, nickname *string, newPassword *string, profileImageURL *string) (*mysql.User, error)
}
