package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/profile/model/interface"

	"gorm.io/gorm"
)

type GetProfileRepository struct {
	GormDB *gorm.DB
}

func NewGetProfileRepository(gormDB *gorm.DB) _interface.IGetProfileRepository {
	return &GetProfileRepository{
		GormDB: gormDB,
	}
}

func (r *GetProfileRepository) GetByUserID(ctx context.Context, userID uint) (*mysql.User, error) {
	var user mysql.User

	result := r.GormDB.WithContext(ctx).
		Where("id = ?", userID).
		First(&user)

	if result.Error != nil {
		return nil, result.Error
	}

	return &user, nil
}
