package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/memo/model/interface"

	"gorm.io/gorm"
)

type DeleteMemoRepository struct {
	GormDB *gorm.DB
}

func NewDeleteMemoRepository(gormDB *gorm.DB) _interface.IDeleteMemoRepository {
	return &DeleteMemoRepository{
		GormDB: gormDB,
	}
}

// Delete 메모 삭제 (Soft Delete)
func (r *DeleteMemoRepository) Delete(ctx context.Context, id uint, userID uint) error {
	result := r.GormDB.WithContext(ctx).
		Where("id = ? AND user_id = ?", id, userID).
		Delete(&mysql.Memo{})

	if result.Error != nil {
		return result.Error
	}

	if result.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}

	return nil
}
