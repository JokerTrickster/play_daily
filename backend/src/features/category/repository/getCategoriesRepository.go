package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/category/model/interface"

	"gorm.io/gorm"
)

type CategoryRepository struct {
	GormDB *gorm.DB
}

func NewCategoryRepository(gormDB *gorm.DB) _interface.ICategoryRepository {
	return &CategoryRepository{
		GormDB: gormDB,
	}
}

// GetAll 모든 카테고리 조회 (display_order 오름차순)
func (r *CategoryRepository) GetAll(ctx context.Context) ([]mysql.MemoCategory, error) {
	var categories []mysql.MemoCategory
	result := r.GormDB.WithContext(ctx).
		Order("display_order ASC").
		Find(&categories)

	if result.Error != nil {
		return nil, result.Error
	}

	return categories, nil
}
