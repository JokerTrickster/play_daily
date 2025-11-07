package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"

	"gorm.io/gorm"
)

type GetPublicRoomsRepository struct {
	GormDB *gorm.DB
}

func NewGetPublicRoomsRepository(gormDB *gorm.DB) _interface.IGetPublicRoomsRepository {
	return &GetPublicRoomsRepository{
		GormDB: gormDB,
	}
}

// GetPublicRooms retrieves paginated list of public rooms
func (r *GetPublicRoomsRepository) GetPublicRooms(ctx context.Context, offset, limit int) ([]mysql.Room, int64, error) {
	var rooms []mysql.Room
	var totalCount int64

	// Count total public rooms
	if err := r.GormDB.WithContext(ctx).Model(&mysql.Room{}).
		Where("is_public = ? AND deleted_at IS NULL", true).
		Count(&totalCount).Error; err != nil {
		return nil, 0, err
	}

	// Fetch paginated rooms with owner details
	if err := r.GormDB.WithContext(ctx).
		Where("is_public = ? AND deleted_at IS NULL", true).
		Preload("Owner").
		Order("likes_count DESC").
		Limit(limit).
		Offset(offset).
		Find(&rooms).Error; err != nil {
		return nil, 0, err
	}

	return rooms, totalCount, nil
}

// GetRoomMemberCount retrieves the member count for a specific room
func (r *GetPublicRoomsRepository) GetRoomMemberCount(ctx context.Context, roomID uint) (int64, error) {
	var count int64
	if err := r.GormDB.WithContext(ctx).Model(&mysql.RoomMember{}).
		Where("room_id = ? AND deleted_at IS NULL", roomID).
		Count(&count).Error; err != nil {
		return 0, err
	}
	return count, nil
}
