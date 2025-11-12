package repository

import (
	"context"
	"errors"
	"fmt"
	"main/common/db/mysql"
	_interface "main/features/auth/model/interface"
	"time"

	"gorm.io/gorm"
)

type ReissueAuthRepository struct {
	GormDB *gorm.DB
}

func NewReissueAuthRepository(gormDB *gorm.DB) _interface.IReissueAuthRepository {
	return &ReissueAuthRepository{
		GormDB: gormDB,
	}
}

// GetTokenByRefreshToken retrieves a token by refresh token
func (r *ReissueAuthRepository) GetTokenByRefreshToken(ctx context.Context, refreshToken string) (*mysql.Token, error) {
	var token mysql.Token

	if err := r.GormDB.WithContext(ctx).Where("refresh_token = ? AND revoked_at IS NULL", refreshToken).First(&token).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, fmt.Errorf("token not found or revoked")
		}
		return nil, fmt.Errorf("failed to get token: %w", err)
	}

	// Check if refresh token is expired
	if time.Now().After(token.RefreshTokenExpiresAt) {
		return nil, fmt.Errorf("refresh token expired")
	}

	return &token, nil
}

// RevokeToken revokes a token using refresh token within a transaction
func (r *ReissueAuthRepository) RevokeToken(ctx context.Context, refreshToken string) error {
	now := time.Now()

	result := r.GormDB.WithContext(ctx).Model(&mysql.Token{}).
		Where("refresh_token = ? AND revoked_at IS NULL", refreshToken).
		Update("revoked_at", now)

	if result.Error != nil {
		return fmt.Errorf("failed to revoke token: %w", result.Error)
	}

	if result.RowsAffected == 0 {
		return fmt.Errorf("token not found or already revoked")
	}

	return nil
}

// StoreToken stores access and refresh tokens in the database
func (r *ReissueAuthRepository) StoreToken(ctx context.Context, userID uint, accessToken, refreshToken string, accessTokenExpiresAt, refreshTokenExpiresAt time.Time) error {
	token := &mysql.Token{
		UserID:                userID,
		AccessToken:           accessToken,
		RefreshToken:          refreshToken,
		AccessTokenExpiresAt:  accessTokenExpiresAt,
		RefreshTokenExpiresAt: refreshTokenExpiresAt,
		RevokedAt:             nil,
	}

	if err := r.GormDB.WithContext(ctx).Create(token).Error; err != nil {
		return fmt.Errorf("failed to store token: %w", err)
	}

	return nil
}
