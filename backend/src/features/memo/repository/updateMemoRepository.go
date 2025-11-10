package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/memo/model/interface"

	"gorm.io/gorm"
)

type UpdateMemoRepository struct {
	GormDB *gorm.DB
}

func NewUpdateMemoRepository(gormDB *gorm.DB) _interface.IUpdateMemoRepository {
	return &UpdateMemoRepository{
		GormDB: gormDB,
	}
}

// Update 메모 수정
func (r *UpdateMemoRepository) Update(ctx context.Context, id uint, userID uint, memo *mysql.Memo) error {
	result := r.GormDB.WithContext(ctx).
		Model(&mysql.Memo{}).
		Where("id = ? AND user_id = ?", id, userID).
		Updates(memo)

	if result.Error != nil {
		return result.Error
	}

	if result.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}

	return nil
}

// GetByID 특정 메모 조회 (업데이트 후 조회용)
func (r *UpdateMemoRepository) GetByID(ctx context.Context, id uint, userID uint) (*mysql.Memo, error) {
	var memo mysql.Memo
	result := r.GormDB.WithContext(ctx).
		Preload("Categories").
		Where("id = ? AND user_id = ?", id, userID).
		First(&memo)

	if result.Error != nil {
		return nil, result.Error
	}

	return &memo, nil
}

// CheckUserLikedMemo 사용자가 특정 메모를 좋아요 했는지 확인
func (r *UpdateMemoRepository) CheckUserLikedMemo(ctx context.Context, memoID uint, userID uint) (bool, error) {
	var count int64
	result := r.GormDB.WithContext(ctx).
		Model(&mysql.MemoLike{}).
		Where("memo_id = ? AND user_id = ?", memoID, userID).
		Count(&count)

	if result.Error != nil {
		return false, result.Error
	}

	return count > 0, nil
}

// UpdateCategories 메모의 카테고리를 업데이트 (기존 카테고리를 삭제하고 새로운 카테고리로 교체)
func (r *UpdateMemoRepository) UpdateCategories(ctx context.Context, memoID uint, categoryIDs []uint) error {
	return r.GormDB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 1. 기존 카테고리 연결 삭제
		if err := tx.Where("memo_id = ?", memoID).Delete(&mysql.MemoCategorySelection{}).Error; err != nil {
			return err
		}

		// 2. 새로운 카테고리 연결 생성
		for _, categoryID := range categoryIDs {
			selection := &mysql.MemoCategorySelection{
				MemoID:     memoID,
				CategoryID: categoryID,
			}
			if err := tx.Create(selection).Error; err != nil {
				return err
			}
		}

		return nil
	})
}
