package _interface

import (
	"context"
	"main/common/db/mysql"
)

type ISignInAuthRepository interface {
	CheckPassword(ctx context.Context, id, password string) error
}

type ISignUpAuthRepository interface {
	CheckAccountIDDuplicate(ctx context.Context, accountID string) error
	CreateUser(ctx context.Context, userDTO *mysql.User) error
}
