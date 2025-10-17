package repository

import (
	"context"
	"errors"
	"main/common/db/mysql"
	_interface "main/features/auth/model/interface"

	"gorm.io/gorm"
)

type SignUpAuthRepository struct {
	GormDB *gorm.DB
}

func NewSignUpAuthRepository(gormDB *gorm.DB) _interface.ISignUpAuthRepository {
	return &SignUpAuthRepository{
		GormDB: gormDB,
	}
}

func (r *SignUpAuthRepository) CheckAccountIDDuplicate(ctx context.Context, accountID string) error {
	var user mysql.User

	// account_id로 사용자 조회
	result := r.GormDB.WithContext(ctx).
		Where("account_id = ?", accountID).
		First(&user)

	// 사용자가 있으면 중복 에러 반환
	if result.Error == nil {
		return errors.New("account ID already exists")
	}

	// 사용자가 없으면 nil 반환
	return nil
}

func (r *SignUpAuthRepository) CreateUser(ctx context.Context, userDTO *mysql.User) error {
	// 사용자 생성
	result := r.GormDB.WithContext(ctx).Create(userDTO)

	// 에러 반환
	return result.Error
}
