package repository

import (
	"context"
	"errors"
	"fmt"
	"main/common/db/mysql"
	_interface "main/features/auth/model/interface"

	"gorm.io/gorm"
)

type SignInAuthRepository struct {
	GormDB *gorm.DB
}

func NewSignInAuthRepository(gormDB *gorm.DB) _interface.ISignInAuthRepository {
	return &SignInAuthRepository{
		GormDB: gormDB,
	}
}

// CheckPassword account_id와 password로 사용자 조회
func (r *SignInAuthRepository) CheckPassword(ctx context.Context, accountID, password string) (*mysql.User, error) {
	var user mysql.User

	// account_id와 password로 사용자 조회
	result := r.GormDB.WithContext(ctx).
		Where("account_id = ? AND password = ?", accountID, password).
		First(&user)

	// 에러 체크
	if result.Error != nil {
		if errors.Is(result.Error, gorm.ErrRecordNotFound) {
			return nil, fmt.Errorf("invalid account ID or password")
		}
		return nil, result.Error
	}

	// 사용자 정보 반환
	return &user, nil
}
