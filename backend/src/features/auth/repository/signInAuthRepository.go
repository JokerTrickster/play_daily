package repository

import (
	"context"
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
func (r *SignInAuthRepository) CheckPassword(ctx context.Context, accountID, password string) error {
	var user mysql.User

	// account_id와 password로 사용자 조회
	result := r.GormDB.WithContext(ctx).
		Where("account_id = ? AND password = ?", accountID, password).
		First(&user)

	// 사용자가 없으면 에러 반환
	if result.Error != nil {
		return result.Error
	}

	// 사용자가 있으면 nil 반환
	return nil
}
