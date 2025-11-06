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

// CreateWithCategories 메모와 카테고리 연결을 트랜잭션으로 생성
func (r *CreateMemoRepository) CreateWithCategories(ctx context.Context, memo *mysql.Memo, categoryIDs []uint) error {
	return r.GormDB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 1. 메모 생성
		if err := tx.Create(memo).Error; err != nil {
			return err
		}

		// 2. 카테고리 연결 생성
		for _, categoryID := range categoryIDs {
			selection := &mysql.MemoCategorySelection{
				MemoID:     memo.ID,
				CategoryID: categoryID,
			}
			if err := tx.Create(selection).Error; err != nil {
				return err
			}
		}

		return nil
	})
}
