package repository

import (
	"context"
	"errors"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"
	"main/features/room/model/response"

	"gorm.io/gorm"
)

type JoinRoomRepository struct {
	DB *gorm.DB
}

func NewJoinRoomRepository(db *gorm.DB) _interface.IJoinRoomRepository {
	return &JoinRoomRepository{
		DB: db,
	}
}

// GetRoomByID retrieves room information by room ID
func (r *JoinRoomRepository) GetRoomByID(ctx context.Context, roomID uint) (*response.ResRoom, error) {
	var room mysql.Room
	if err := r.DB.WithContext(ctx).Where("id = ?", roomID).First(&room).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("room not found")
		}
		return nil, err
	}

	return &response.ResRoom{
		ID:           room.ID,
		OwnerID:      room.OwnerUserID,
		RoomPassword: "", // Password should not be returned
	}, nil
}

// VerifyRoomPassword verifies the room password by checking the owner's room password
func (r *JoinRoomRepository) VerifyRoomPassword(ctx context.Context, roomID uint, password string) (bool, error) {
	var room mysql.Room
	if err := r.DB.WithContext(ctx).Preload("Owner").Where("id = ?", roomID).First(&room).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return false, errors.New("room not found")
		}
		return false, err
	}

	// Check if room has an owner
	if room.Owner == nil {
		return false, errors.New("room owner not found")
	}

	// Compare password with owner's room password
	return room.Owner.RoomPassword == password, nil
}

// UpdateUserDefaultRoom updates user's default room ID
func (r *JoinRoomRepository) UpdateUserDefaultRoom(ctx context.Context, userID uint, roomID uint) error {
	result := r.DB.WithContext(ctx).Model(&mysql.User{}).Where("id = ?", userID).Update("default_room_id", roomID)
	if result.Error != nil {
		return result.Error
	}
	if result.RowsAffected == 0 {
		return errors.New("user not found")
	}
	return nil
}
