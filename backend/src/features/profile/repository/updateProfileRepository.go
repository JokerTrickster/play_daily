package repository

import (
	"context"
	"errors"
	"fmt"
	"main/common/db/mysql"
	_interface "main/features/profile/model/interface"

	"gorm.io/gorm"
)

type UpdateProfileRepository struct {
	GormDB *gorm.DB
}

func NewUpdateProfileRepository(gormDB *gorm.DB) _interface.IUpdateProfileRepository {
	return &UpdateProfileRepository{
		GormDB: gormDB,
	}
}

func (r *UpdateProfileRepository) UpdateProfile(ctx context.Context, userID uint, currentPassword string, nickname *string, newPassword *string, profileImageURL *string) (*mysql.User, error) {
	var user mysql.User

	// 트랜잭션 시작
	err := r.GormDB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 1. 사용자 조회
		result := tx.Where("id = ?", userID).First(&user)
		if result.Error != nil {
			if errors.Is(result.Error, gorm.ErrRecordNotFound) {
				return fmt.Errorf("user not found")
			}
			return result.Error
		}

		// 2. 현재 비밀번호 검증
		if user.Password != currentPassword {
			return fmt.Errorf("incorrect current password")
		}

		// 3. 필드 업데이트 (제공된 필드만)
		if nickname != nil {
			user.Nickname = *nickname
		}

		if newPassword != nil {
			user.Password = *newPassword
		}

		if profileImageURL != nil {
			user.ProfileImageURL = profileImageURL
		}

		// 4. 사용자 정보 저장
		if err := tx.Save(&user).Error; err != nil {
			return err
		}

		return nil
	})

	if err != nil {
		return nil, err
	}

	return &user, nil
}
