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

// GetMemoIDByCommentID 댓글 ID로 메모 ID 조회
func (r *DeleteCommentRepository) GetMemoIDByCommentID(ctx context.Context, commentID uint) (uint, error) {
	var comment mysql.Comment
	err := r.GormDB.WithContext(ctx).
		Select("memo_id").
		Where("id = ?", commentID).
		First(&comment).Error

	if err != nil {
		return 0, err
	}

	return comment.MemoID, nil
}

// UpdateMemoRating 메모의 평점을 댓글들의 평균 평점으로 업데이트
func (r *DeleteCommentRepository) UpdateMemoRating(ctx context.Context, memoID uint) error {
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
