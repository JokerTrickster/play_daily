package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/comment/model/interface"

	"gorm.io/gorm"
)

type DeleteCommentRepository struct {
	GormDB *gorm.DB
}

func NewDeleteCommentRepository(gormDB *gorm.DB) _interface.IDeleteCommentRepository {
	return &DeleteCommentRepository{
		GormDB: gormDB,
	}
}

// Delete 댓글 삭제 (본인 댓글만 삭제 가능)
func (r *DeleteCommentRepository) Delete(ctx context.Context, id uint, userID uint) error {
	result := r.GormDB.WithContext(ctx).
		Where("id = ? AND user_id = ?", id, userID).
		Delete(&mysql.Comment{})

	if result.Error != nil {
		return result.Error
	}

	if result.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}

	return nil
}
