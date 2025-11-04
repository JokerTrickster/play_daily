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
// 다른 사용자의 방에 있는 메모도 조회 가능하도록 user_id 필터 제거
func (r *GetMemoRepository) GetByID(ctx context.Context, id uint, userID uint) (*mysql.Memo, error) {
	var memo mysql.Memo
	result := r.GormDB.WithContext(ctx).
		Preload("Comments.User").
		Where("id = ?", id).
		First(&memo)

	if result.Error != nil {
		return nil, result.Error
	}

	return &memo, nil
}

// GetListByUserID 사용자의 메모 목록 조회 (Room ID, 위시리스트 필터, 페이지네이션 옵션 포함)
func (r *GetMemoRepository) GetListByUserID(ctx context.Context, userID uint, roomID *uint, isWishlist *bool, offset int, limit int) ([]mysql.Memo, error) {
	var memos []mysql.Memo
	query := r.GormDB.WithContext(ctx)

	// roomID 필터 적용
	// room_id가 지정된 경우: 해당 방의 모든 메모 조회 (다른 사용자가 작성한 메모도 포함)
	// room_id가 없는 경우: 현재 사용자가 작성한 메모만 조회
	if roomID != nil {
		query = query.Where("room_id = ?", *roomID)
	} else {
		query = query.Where("user_id = ?", userID)
	}

	// isWishlist 필터 적용
	if isWishlist != nil {
		query = query.Where("is_wishlist = ?", *isWishlist)
	}

	result := query.
		Order("is_pinned DESC, created_at DESC").
		Offset(offset).
		Limit(limit).
		Find(&memos)

	if result.Error != nil {
		return nil, result.Error
	}

	return memos, nil
}

// CountByUserID 사용자의 메모 총 개수 조회 (필터 조건 포함)
func (r *GetMemoRepository) CountByUserID(ctx context.Context, userID uint, roomID *uint, isWishlist *bool) (int64, error) {
	var count int64
	query := r.GormDB.WithContext(ctx).Model(&mysql.Memo{})

	// roomID 필터 적용
	// room_id가 지정된 경우: 해당 방의 모든 메모 카운트 (다른 사용자가 작성한 메모도 포함)
	// room_id가 없는 경우: 현재 사용자가 작성한 메모만 카운트
	if roomID != nil {
		query = query.Where("room_id = ?", *roomID)
	} else {
		query = query.Where("user_id = ?", userID)
	}

	// isWishlist 필터 적용
	if isWishlist != nil {
		query = query.Where("is_wishlist = ?", *isWishlist)
	}

	result := query.Count(&count)

	if result.Error != nil {
		return 0, result.Error
	}

	return count, nil
}

// CheckUserLikedMemo 사용자가 특정 메모를 좋아요 했는지 확인
func (r *GetMemoRepository) CheckUserLikedMemo(ctx context.Context, memoID uint, userID uint) (bool, error) {
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
