package repository

import (
	"context"
	"errors"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"

	"gorm.io/gorm"
)

type JoinRoomRepository struct {
	GormDB *gorm.DB
}

func NewJoinRoomRepository(db *gorm.DB) _interface.IJoinRoomRepository {
	return &JoinRoomRepository{
		GormDB: db,
	}
}

// GetRoomByRoomCode retrieves room information by room code
func (r *JoinRoomRepository) GetRoomByRoomCode(ctx context.Context, roomCode string) (*mysql.Room, error) {
	var room mysql.Room
	if err := r.GormDB.WithContext(ctx).
		Where("room_code = ? AND deleted_at IS NULL", roomCode).
		First(&room).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("room not found")
		}
		return nil, err
	}
	return &room, nil
}

// FindExistingMember checks if a user is already a member of the room
func (r *JoinRoomRepository) FindExistingMember(ctx context.Context, roomID uint, userID uint) (*mysql.RoomMember, error) {
	var member mysql.RoomMember
	if err := r.GormDB.WithContext(ctx).
		Where("room_id = ? AND user_id = ? AND deleted_at IS NULL", roomID, userID).
		First(&member).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &member, nil
}

// CreateRoomMember adds a new member to the room
func (r *JoinRoomRepository) CreateRoomMember(ctx context.Context, member *mysql.RoomMember) error {
	if err := r.GormDB.WithContext(ctx).Create(member).Error; err != nil {
		return err
	}
	return nil
}

// GetRoomMemberCount retrieves the member count for a specific room
func (r *JoinRoomRepository) GetRoomMemberCount(ctx context.Context, roomID uint) (int64, error) {
	var count int64
	if err := r.GormDB.WithContext(ctx).Model(&mysql.RoomMember{}).
		Where("room_id = ? AND deleted_at IS NULL", roomID).
		Count(&count).Error; err != nil {
		return 0, err
	}
	return count, nil
}
