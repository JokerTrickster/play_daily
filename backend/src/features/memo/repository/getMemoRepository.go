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

// GetByID 특정 메모 조회
func (r *GetMemoRepository) GetByID(ctx context.Context, id uint, userID uint) (*mysql.Memo, error) {
	var memo mysql.Memo
	result := r.GormDB.WithContext(ctx).
		Where("id = ? AND user_id = ?", id, userID).
		First(&memo)

	if result.Error != nil {
		return nil, result.Error
	}

	return &memo, nil
}

// GetListByUserID 사용자의 메모 목록 조회
func (r *GetMemoRepository) GetListByUserID(ctx context.Context, userID uint) ([]mysql.Memo, error) {
	var memos []mysql.Memo
	result := r.GormDB.WithContext(ctx).
		Where("user_id = ?", userID).
		Order("is_pinned DESC, created_at DESC").
		Find(&memos)

	if result.Error != nil {
		return nil, result.Error
	}

	return memos, nil
}

// GetListWithFilters 필터 조건을 적용하여 메모 목록 조회
func (r *GetMemoRepository) GetListWithFilters(ctx context.Context, userID uint, roomID *uint, isWishlist *bool) ([]mysql.Memo, error) {
	var memos []mysql.Memo
	query := r.GormDB.WithContext(ctx)

	// 기본 사용자 필터
	query = query.Where("user_id = ?", userID)

	// roomID 필터 (다른 사용자의 방 조회)
	if roomID != nil {
		query = query.Where("user_id = ?", *roomID)
	}

	// isWishlist 필터
	if isWishlist != nil {
		query = query.Where("is_wishlist = ?", *isWishlist)
	}

	result := query.Order("is_pinned DESC, created_at DESC").Find(&memos)

	if result.Error != nil {
		return nil, result.Error
	}

	return memos, nil
}
