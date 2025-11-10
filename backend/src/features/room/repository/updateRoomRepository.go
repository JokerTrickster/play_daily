package repository

import (
	"context"
	"main/common/db/mysql"

	"gorm.io/gorm"
)

type UpdateRoomRepository struct {
	db *gorm.DB
}

func NewUpdateRoomRepository(db *gorm.DB) *UpdateRoomRepository {
	return &UpdateRoomRepository{db: db}
}

func (r *UpdateRoomRepository) UpdateRoomSettings(ctx context.Context, roomID int64, isPublic *bool, roomPassword string) error {
	updates := make(map[string]interface{})

	if isPublic != nil {
		updates["is_public"] = *isPublic
	}

	if roomPassword != "" {
		updates["room_password"] = roomPassword
	}

	if len(updates) == 0 {
		return nil // 업데이트할 내용이 없음
	}

	result := r.db.WithContext(ctx).
		Model(&mysql.Room{}).
		Where("id = ? AND deleted_at IS NULL", roomID).
		Updates(updates)

	if result.Error != nil {
		return result.Error
	}

	if result.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}

	return nil
}

func (r *UpdateRoomRepository) IsOwner(ctx context.Context, roomID, userID int64) (bool, error) {
	var count int64
	err := r.db.WithContext(ctx).
		Model(&mysql.RoomMember{}).
		Where("room_id = ? AND user_id = ? AND permission = ? AND deleted_at IS NULL", roomID, userID, "OWNER").
		Count(&count).Error

	return count > 0, err
}
