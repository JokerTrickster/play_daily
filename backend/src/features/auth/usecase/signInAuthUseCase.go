package usecase

import (
	"context"
	_interface "main/features/auth/model/interface"
	"main/features/auth/model/request"
	"time"
)

type SignInAuthUseCase struct {
	Repository     _interface.ISignInAuthRepository
	ContextTimeout time.Duration
}

func NewSignInAuthUseCase(repo _interface.ISignInAuthRepository, timeout time.Duration) _interface.ISignInAuthUseCase {
	return &SignInAuthUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

func (uc *SignInAuthUseCase) SignIn(ctx context.Context, req request.ReqSignIn) error {
	_, cancle := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancle()

	err := uc.Repository.CheckPassword(ctx, req.AccountID, req.Password)
	if err != nil {
		return err
	}

	// 액세스토큰 생성
	return nil
}
