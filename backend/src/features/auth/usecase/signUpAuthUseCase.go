package usecase

import (
	"context"
	"errors"
	_interface "main/features/auth/model/interface"
	"main/features/auth/model/request"
	"time"
)

type SignUpAuthUseCase struct {
	Repository     _interface.ISignUpAuthRepository
	ContextTimeout time.Duration
}

func NewSignUpAuthUseCase(repo _interface.ISignUpAuthRepository, timeout time.Duration) _interface.ISignUpAuthUseCase {
	return &SignUpAuthUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

func (uc *SignUpAuthUseCase) SignUp(ctx context.Context, req request.ReqSignUp) error {
	_, cancle := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancle()

	// account_id 중복 체크
	err := uc.Repository.CheckAccountIDDuplicate(ctx, req.AccountID)
	if err != nil {
		return err
	}

	// 인증 코드 검증
	if !ValidateAuthCode(req.AuthCode) {
		return errors.New("invalid authentication code")
	}

	// 사용자 DTO 생성
	userDTO := createUserDTO(req)

	// 디비 저장
	err = uc.Repository.CreateUser(ctx, *userDTO)
	if err != nil {
		return err
	}

	return nil
}
