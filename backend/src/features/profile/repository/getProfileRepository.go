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
		Preload("DefaultRoom"). // DefaultRoom을 preload하여 LikesCount 가져오기
		Where("id = ?", userID).
		First(&user)

	if result.Error != nil {
		return nil, result.Error
	}

	return &user, nil
}

// GetUserMemoLikesCount returns the total number of likes received on all memos created by the user
func (r *GetProfileRepository) GetUserMemoLikesCount(ctx context.Context, userID uint) (uint, error) {
	var totalLikes int64

	// Count all likes on memos where the memo's user_id matches the given userID
	result := r.GormDB.WithContext(ctx).
		Table("memo_likes").
		Joins("JOIN memos ON memos.id = memo_likes.memo_id").
		Where("memos.user_id = ? AND memos.deleted_at IS NULL", userID).
		Count(&totalLikes)

	if result.Error != nil {
		return 0, result.Error
	}

	return uint(totalLikes), nil
}
