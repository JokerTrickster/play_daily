package usecase

import (
	"context"
	"fmt"
	"main/common"
	_interface "main/features/auth/model/interface"
	"main/features/auth/model/request"
	"main/features/auth/model/response"
	"time"

	"github.com/golang-jwt/jwt"
)

type ReissueAuthUseCase struct {
	Repository     _interface.IReissueAuthRepository
	ContextTimeout time.Duration
}

func NewReissueAuthUseCase(repo _interface.IReissueAuthRepository, timeout time.Duration) _interface.IReissueAuthUseCase {
	return &ReissueAuthUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

func (uc *ReissueAuthUseCase) Reissue(ctx context.Context, req request.ReqReissue) (*response.ResReissue, error) {
	_, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// Step 1: Verify refresh token signature BEFORE database query
	token, err := jwt.ParseWithClaims(req.RefreshToken, &common.JwtCustomClaims{}, func(token *jwt.Token) (interface{}, error) {
		return common.RefreshTokenSecretKey, nil
	})
	if err != nil {
		return nil, fmt.Errorf("invalid refresh token signature: %w", err)
	}

	if !token.Valid {
		return nil, fmt.Errorf("invalid refresh token")
	}

	// Extract claims for user info
	claims, ok := token.Claims.(*common.JwtCustomClaims)
	if !ok {
		return nil, fmt.Errorf("failed to extract token claims")
	}

	// Step 2: Verify refresh token exists in database and is not revoked
	storedToken, err := uc.Repository.GetTokenByRefreshToken(ctx, req.RefreshToken)
	if err != nil {
		return nil, fmt.Errorf("refresh token not found or expired: %w", err)
	}

	// Step 3: Revoke the old refresh token
	if err := uc.Repository.RevokeToken(ctx, req.RefreshToken); err != nil {
		return nil, fmt.Errorf("failed to revoke old token: %w", err)
	}

	// Step 4: Generate new token pair
	now := time.Now()
	newAccessToken, accessTokenExpiredAt, err := common.GenerateAccessToken(claims.Email, now, claims.UserID)
	if err != nil {
		return nil, fmt.Errorf("failed to generate access token: %w", err)
	}

	newRefreshToken, refreshTokenExpiredAt, err := common.GenerateRefreshToken(claims.Email, now, claims.UserID)
	if err != nil {
		return nil, fmt.Errorf("failed to generate refresh token: %w", err)
	}

	// Step 5: Store new tokens in database
	accessTokenTime := time.Unix(accessTokenExpiredAt, 0)
	refreshTokenTime := time.Unix(refreshTokenExpiredAt, 0)
	if err := uc.Repository.StoreToken(ctx, storedToken.UserID, newAccessToken, newRefreshToken, accessTokenTime, refreshTokenTime); err != nil {
		return nil, fmt.Errorf("failed to store new tokens: %w", err)
	}

	// Step 6: Return response
	res := &response.ResReissue{
		AccessToken:           newAccessToken,
		RefreshToken:          newRefreshToken,
		AccessTokenExpiredAt:  accessTokenExpiredAt,
		RefreshTokenExpiredAt: refreshTokenExpiredAt,
	}

	return res, nil
}
