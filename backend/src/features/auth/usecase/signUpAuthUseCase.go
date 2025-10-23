package usecase

import (
	"context"
	"errors"
	"main/common"
	_interface "main/features/auth/model/interface"
	"main/features/auth/model/request"
	"main/features/auth/model/response"
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

func (uc *SignUpAuthUseCase) SignUp(ctx context.Context, req request.ReqSignUp) (*response.ResAuth, error) {
	_, cancle := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancle()

	// account_id 중복 체크
	err := uc.Repository.CheckAccountIDDuplicate(ctx, req.AccountID)
	if err != nil {
		return nil, err
	}

	// 인증 코드 검증
	if !ValidateAuthCode(req.AuthCode) {
		return nil, errors.New("invalid authentication code")
	}

	// 사용자 DTO 생성
	userDTO := createUserDTO(req)

	// 디비 저장
	user, err := uc.Repository.CreateUser(ctx, userDTO)
	if err != nil {
		return nil, err
	}

	// JWT 토큰 생성
	accessToken, accessTokenExpiredAt, refreshToken, refreshTokenExpiredAt, err := common.GenerateToken(user.AccountID, user.ID)
	if err != nil {
		return nil, err
	}

	// 응답 생성
	res := &response.ResAuth{
		AccessToken:           accessToken,
		AccessTokenExpiredAt:  accessTokenExpiredAt,
		RefreshToken:          refreshToken,
		RefreshTokenExpiredAt: refreshTokenExpiredAt,
		UserID:                user.ID,
		AccountID:             user.AccountID,
		Nickname:              user.Nickname,
		DefaultRoomID:         user.DefaultRoomID,
	}

	return res, nil
}
