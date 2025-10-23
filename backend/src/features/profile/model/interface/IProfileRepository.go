package _interface

import (
	"context"
	"main/common/db/mysql"
)

type IGetProfileRepository interface {
	GetByUserID(ctx context.Context, userID uint) (*mysql.User, error)
}
