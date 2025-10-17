package _interface

import (
	"context"
	"main/features/auth/model/request"
)

type ISignInAuthUseCase interface {
	SignIn(ctx context.Context, req request.ReqSignIn) error
}
