package repository

import (
	"context"
	"main/common/db/mysql"

	"gorm.io/gorm"
)

type UpdatePermissionRepository struct {
	db *gorm.DB
}

func NewUpdatePermissionRepository(db *gorm.DB) *UpdatePermissionRepository {
	return &UpdatePermissionRepository{db: db}
}

func (r *UpdatePermissionRepository) UpdatePermission(ctx context.Context, roomID, userID int64, permission string) error {
	// 권한 업데이트
	result := r.db.WithContext(ctx).
		Model(&mysql.RoomMember{}).
		Where("room_id = ? AND user_id = ? AND deleted_at IS NULL", roomID, userID).
		Update("permission", permission)

	if result.Error != nil {
		return result.Error
	}

	if result.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}

	return nil
}

func (r *UpdatePermissionRepository) IsOwner(ctx context.Context, roomID, userID int64) (bool, error) {
	var count int64
	err := r.db.WithContext(ctx).
		Model(&mysql.RoomMember{}).
		Where("room_id = ? AND user_id = ? AND permission = ? AND deleted_at IS NULL", roomID, userID, "OWNER").
		Count(&count).Error

	return count > 0, err
}
