package repository

import (
	"context"
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

	// 사용자가 없으면 에러 반환
	fmt.Println("result.Error:", result.Error)
	if result.Error.Error() == gorm.ErrRecordNotFound.Error() {
		return nil, fmt.Errorf("invalid account ID or password")
	}
	// 사용자 정보 반환
	return &user, nil
}
