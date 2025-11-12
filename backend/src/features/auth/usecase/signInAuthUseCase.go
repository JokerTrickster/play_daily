package usecase

import (
	"context"
	"main/common"
	_interface "main/features/auth/model/interface"
	"main/features/auth/model/request"
	"main/features/auth/model/response"
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

func (uc *SignInAuthUseCase) SignIn(ctx context.Context, req request.ReqSignIn) (*response.ResAuth, error) {
	_, cancle := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancle()

	// 사용자 인증
	user, err := uc.Repository.CheckPassword(ctx, req.AccountID, req.Password)
	if err != nil {
		return nil, err
	}

	// JWT 토큰 생성
	accessToken, accessTokenExpiredAt, refreshToken, refreshTokenExpiredAt, err := common.GenerateToken(user.AccountID, user.ID)
	if err != nil {
		return nil, err
	}

	// 토큰을 데이터베이스에 저장
	accessTokenTime := time.Unix(accessTokenExpiredAt, 0)
	refreshTokenTime := time.Unix(refreshTokenExpiredAt, 0)
	if err := uc.Repository.StoreToken(ctx, user.ID, accessToken, refreshToken, accessTokenTime, refreshTokenTime); err != nil {
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
		RoomPassword:          user.RoomPassword,
	}

	return res, nil
}
