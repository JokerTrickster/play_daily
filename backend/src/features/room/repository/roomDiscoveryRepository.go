package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"

	"gorm.io/gorm"
)

type RoomDiscoveryRepository struct {
	GormDB *gorm.DB
}

func NewRoomDiscoveryRepository(gormDB *gorm.DB) _interface.IRoomDiscoveryRepository {
	return &RoomDiscoveryRepository{
		GormDB: gormDB,
	}
}

// GetPopularRooms retrieves public rooms sorted by likes_count DESC, created_at DESC
// Uses composite index: idx_rooms_discovery (is_public, likes_count DESC, created_at DESC)
func (r *RoomDiscoveryRepository) GetPopularRooms(ctx context.Context, offset int, limit int) ([]mysql.Room, error) {
	var rooms []mysql.Room
	result := r.GormDB.WithContext(ctx).
		Where("is_public = ?", true).
		Order("likes_count DESC, created_at DESC").
		Offset(offset).
		Limit(limit).
		Find(&rooms)

	if result.Error != nil {
		return nil, result.Error
	}

	return rooms, nil
}

// GetRecentRooms retrieves public rooms sorted by owner's user.created_at ASC (oldest users first)
// Joins with users table to get owner creation date
func (r *RoomDiscoveryRepository) GetRecentRooms(ctx context.Context, offset int, limit int) ([]mysql.Room, error) {
	var rooms []mysql.Room
	result := r.GormDB.WithContext(ctx).
		Joins("JOIN users ON users.id = rooms.owner_user_id").
		Where("rooms.is_public = ?", true).
		Order("users.created_at ASC").
		Offset(offset).
		Limit(limit).
		Find(&rooms)

	if result.Error != nil {
		return nil, result.Error
	}

	return rooms, nil
}

// CountPublicRooms returns total count of public rooms
func (r *RoomDiscoveryRepository) CountPublicRooms(ctx context.Context) (int64, error) {
	var count int64
	result := r.GormDB.WithContext(ctx).
		Model(&mysql.Room{}).
		Where("is_public = ?", true).
		Count(&count)

	if result.Error != nil {
		return 0, result.Error
	}

	return count, nil
}
