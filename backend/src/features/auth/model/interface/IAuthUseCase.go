package _interface

import (
	"context"
	"main/features/auth/model/request"
	"main/features/auth/model/response"
)

type ISignInAuthUseCase interface {
	SignIn(ctx context.Context, req request.ReqSignIn) (*response.ResAuth, error)
}

type ISignUpAuthUseCase interface {
	SignUp(ctx context.Context, req request.ReqSignUp) (*response.ResAuth, error)
}
