package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/comment/model/interface"

	"gorm.io/gorm"
)

type GetCommentRepository struct {
	GormDB *gorm.DB
}

func NewGetCommentRepository(gormDB *gorm.DB) _interface.IGetCommentRepository {
	return &GetCommentRepository{
		GormDB: gormDB,
	}
}

// GetListByMemoID 특정 메모의 댓글 목록 조회
func (r *GetCommentRepository) GetListByMemoID(ctx context.Context, memoID uint) ([]mysql.Comment, error) {
	var comments []mysql.Comment
	result := r.GormDB.WithContext(ctx).
		Preload("User").
		Where("memo_id = ?", memoID).
		Order("created_at DESC").
		Find(&comments)

	if result.Error != nil {
		return nil, result.Error
	}

	return comments, nil
}

// GetByID 특정 댓글 조회
func (r *GetCommentRepository) GetByID(ctx context.Context, id uint) (*mysql.Comment, error) {
	var comment mysql.Comment
	result := r.GormDB.WithContext(ctx).
		Preload("User").
		Where("id = ?", id).
		First(&comment)

	if result.Error != nil {
		return nil, result.Error
	}

	return &comment, nil
}
