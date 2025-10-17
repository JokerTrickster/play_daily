package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/memo/model/interface"

	"gorm.io/gorm"
)

type CreateMemoRepository struct {
	GormDB *gorm.DB
}

func NewCreateMemoRepository(gormDB *gorm.DB) _interface.ICreateMemoRepository {
	return &CreateMemoRepository{
		GormDB: gormDB,
	}
}

// Create 메모 생성
func (r *CreateMemoRepository) Create(ctx context.Context, memo *mysql.Memo) error {
	result := r.GormDB.WithContext(ctx).Create(memo)
	return result.Error
}
