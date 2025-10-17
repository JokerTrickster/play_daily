package _interface

import "context"

type ISignInAuthRepository interface {
	CheckPassword(ctx context.Context, id, password string) error
}
