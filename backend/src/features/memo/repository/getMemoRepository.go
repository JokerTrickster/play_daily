package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/memo/model/interface"

	"gorm.io/gorm"
)

type GetMemoRepository struct {
	GormDB *gorm.DB
}

func NewGetMemoRepository(gormDB *gorm.DB) _interface.IGetMemoRepository {
	return &GetMemoRepository{
		GormDB: gormDB,
	}
}

// GetByID 특정 메모 조회 (댓글 포함)
func (r *GetMemoRepository) GetByID(ctx context.Context, id uint, userID uint) (*mysql.Memo, error) {
	var memo mysql.Memo
	result := r.GormDB.WithContext(ctx).
		Preload("Comments.User").
		Where("id = ? AND user_id = ?", id, userID).
		First(&memo)

	if result.Error != nil {
		return nil, result.Error
	}

	return &memo, nil
}

// GetListByUserID 사용자의 메모 목록 조회 (위시리스트 필터 옵션 포함)
func (r *GetMemoRepository) GetListByUserID(ctx context.Context, userID uint, isWishlist *bool) ([]mysql.Memo, error) {
	var memos []mysql.Memo
	query := r.GormDB.WithContext(ctx).Where("user_id = ?", userID)

	// isWishlist 필터 적용
	if isWishlist != nil {
		query = query.Where("is_wishlist = ?", *isWishlist)
	}

	result := query.Order("is_pinned DESC, created_at DESC").Find(&memos)

	if result.Error != nil {
		return nil, result.Error
	}

	return memos, nil
}
