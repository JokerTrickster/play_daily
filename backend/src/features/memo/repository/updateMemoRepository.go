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

// UpdateMemoWithCategories 메모와 카테고리를 단일 트랜잭션으로 업데이트 (원자적 연산)
func (r *UpdateMemoRepository) UpdateMemoWithCategories(ctx context.Context, memoID uint, userID uint, memo *mysql.Memo, categoryIDs []uint) error {
	return r.GormDB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 1. 메모 업데이트 (map을 사용하여 zero value도 업데이트)
		updates := map[string]interface{}{}

		// Title은 항상 업데이트 (빈 문자열도 허용)
		updates["title"] = memo.Title
		// Rating은 항상 업데이트 (0~5 범위)
		updates["rating"] = memo.Rating
		// Boolean 필드는 항상 업데이트
		updates["is_pinned"] = memo.IsPinned
		updates["is_wishlist"] = memo.IsWishlist

		// Pointer 필드는 nil이 아닐 때만 업데이트
		if memo.ImageURL != "" {
			updates["image_url"] = memo.ImageURL
		}
		if memo.Latitude != nil {
			updates["latitude"] = memo.Latitude
		}
		if memo.Longitude != nil {
			updates["longitude"] = memo.Longitude
		}
		if memo.LocationName != nil {
			updates["location_name"] = memo.LocationName
		}
		if memo.BusinessName != nil {
			updates["business_name"] = memo.BusinessName
		}
		if memo.BusinessPhone != nil {
			updates["business_phone"] = memo.BusinessPhone
		}
		if memo.BusinessAddress != nil {
			updates["business_address"] = memo.BusinessAddress
		}

		result := tx.Model(&mysql.Memo{}).
			Where("id = ? AND user_id = ?", memoID, userID).
			Updates(updates)

		if result.Error != nil {
			return result.Error
		}

		if result.RowsAffected == 0 {
			return gorm.ErrRecordNotFound
		}

		// 2. 카테고리 업데이트 (same transaction)
		if len(categoryIDs) > 0 {
			// 2-1. 기존 카테고리 연결 삭제
			if err := tx.Where("memo_id = ?", memoID).Delete(&mysql.MemoCategorySelection{}).Error; err != nil {
				return err
			}

			// 2-2. 새로운 카테고리 연결 생성
			for _, categoryID := range categoryIDs {
				selection := &mysql.MemoCategorySelection{
					MemoID:     memoID,
					CategoryID: categoryID,
				}
				if err := tx.Create(selection).Error; err != nil {
					return err
				}
			}
		}

		return nil
	})
}
