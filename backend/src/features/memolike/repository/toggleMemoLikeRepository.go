package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/memolike/model/interface"

	"gorm.io/gorm"
)

type ToggleMemoLikeRepository struct {
	GormDB *gorm.DB
}

func NewToggleMemoLikeRepository(gormDB *gorm.DB) _interface.IToggleMemoLikeRepository {
	return &ToggleMemoLikeRepository{
		GormDB: gormDB,
	}
}

// CheckMemoLikeExists 메모 좋아요 존재 여부 확인
func (r *ToggleMemoLikeRepository) CheckMemoLikeExists(ctx context.Context, memoID uint, userID uint) (bool, error) {
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

// CreateMemoLike 메모 좋아요 추가
func (r *ToggleMemoLikeRepository) CreateMemoLike(ctx context.Context, memoID uint, userID uint) error {
	memoLike := mysql.MemoLike{
		MemoID: memoID,
		UserID: userID,
	}

	result := r.GormDB.WithContext(ctx).Create(&memoLike)
	return result.Error
}

// DeleteMemoLike 메모 좋아요 제거
func (r *ToggleMemoLikeRepository) DeleteMemoLike(ctx context.Context, memoID uint, userID uint) error {
	result := r.GormDB.WithContext(ctx).
		Where("memo_id = ? AND user_id = ?", memoID, userID).
		Delete(&mysql.MemoLike{})

	if result.Error != nil {
		return result.Error
	}

	if result.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}

	return nil
}

// IncrementMemoLikesCount 메모 좋아요 개수 증가
func (r *ToggleMemoLikeRepository) IncrementMemoLikesCount(ctx context.Context, memoID uint) error {
	result := r.GormDB.WithContext(ctx).
		Model(&mysql.Memo{}).
		Where("id = ?", memoID).
		UpdateColumn("likes_count", gorm.Expr("likes_count + ?", 1))

	return result.Error
}

// DecrementMemoLikesCount 메모 좋아요 개수 감소
func (r *ToggleMemoLikeRepository) DecrementMemoLikesCount(ctx context.Context, memoID uint) error {
	result := r.GormDB.WithContext(ctx).
		Model(&mysql.Memo{}).
		Where("id = ? AND likes_count > 0", memoID).
		UpdateColumn("likes_count", gorm.Expr("likes_count - ?", 1))

	return result.Error
}
