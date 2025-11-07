package repository

import (
	"context"
	"errors"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"

	"gorm.io/gorm"
)

type UpdateRoomPrivacyRepository struct {
	GormDB *gorm.DB
}

func NewUpdateRoomPrivacyRepository(gormDB *gorm.DB) _interface.IUpdateRoomPrivacyRepository {
	return &UpdateRoomPrivacyRepository{
		GormDB: gormDB,
	}
}

// GetRoomByID retrieves a room by its ID
func (r *UpdateRoomPrivacyRepository) GetRoomByID(ctx context.Context, roomID uint) (*mysql.Room, error) {
	var room mysql.Room
	result := r.GormDB.WithContext(ctx).
		Where("id = ? AND deleted_at IS NULL", roomID).
		First(&room)

	if result.Error != nil {
		if errors.Is(result.Error, gorm.ErrRecordNotFound) {
			return nil, errors.New("room not found")
		}
		return nil, result.Error
	}

	return &room, nil
}

// UpdateRoom updates the room's privacy settings
func (r *UpdateRoomPrivacyRepository) UpdateRoom(ctx context.Context, room *mysql.Room) error {
	result := r.GormDB.WithContext(ctx).Save(room)
	if result.Error != nil {
		return result.Error
	}
	return nil
}
