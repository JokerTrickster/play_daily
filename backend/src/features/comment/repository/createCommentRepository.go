package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/comment/model/interface"

	"gorm.io/gorm"
)

type CreateCommentRepository struct {
	GormDB *gorm.DB
}

func NewCreateCommentRepository(gormDB *gorm.DB) _interface.ICreateCommentRepository {
	return &CreateCommentRepository{
		GormDB: gormDB,
	}
}

func (r *CreateCommentRepository) Create(ctx context.Context, comment *mysql.Comment) error {
	result := r.GormDB.WithContext(ctx).Create(comment)
	return result.Error
}

// UpdateMemoRating 메모의 평점을 댓글들의 평균 평점으로 업데이트
func (r *CreateCommentRepository) UpdateMemoRating(ctx context.Context, memoID uint) error {
	// 해당 메모의 모든 댓글의 평균 평점 계산
	var avgRating float64
	err := r.GormDB.WithContext(ctx).
		Model(&mysql.Comment{}).
		Where("memo_id = ? AND rating > 0", memoID).
		Select("COALESCE(AVG(rating), 0)").
		Scan(&avgRating).Error

	if err != nil {
		return err
	}

	// 메모의 평점 업데이트
	err = r.GormDB.WithContext(ctx).
		Model(&mysql.Memo{}).
		Where("id = ?", memoID).
		Update("rating", uint8(avgRating+0.5)). // 반올림
		Error

	return err
}
