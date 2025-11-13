package repository

import (
	"context"
	"errors"

	"main/features/room/model/response"
	"main/common/db/mysql"

	"gorm.io/gorm"
)

type GetRoomDetailRepository struct {
	db *gorm.DB
}

func NewGetRoomDetailRepository(db *gorm.DB) *GetRoomDetailRepository {
	return &GetRoomDetailRepository{db: db}
}

// GetRoomDetail retrieves room detail with owner information (bio)
func (r *GetRoomDetailRepository) GetRoomDetail(ctx context.Context, roomID uint) (*response.ResRoomDetail, error) {
	var room mysql.Room
	var owner mysql.User

	// Get room with owner information
	if err := r.db.WithContext(ctx).
		Preload("Owner").
		Where("id = ?", roomID).
		First(&room).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("room not found")
		}
		return nil, err
	}

	// Get owner details
	if err := r.db.WithContext(ctx).
		Where("id = ?", room.OwnerUserID).
		First(&owner).Error; err != nil {
		return nil, err
	}

	return &response.ResRoomDetail{
		ID:         room.ID,
		Name:       room.Name,
		RoomCode:   room.RoomCode,
		IsPublic:   room.IsPublic,
		LikesCount: room.LikesCount,
		OwnerID:    room.OwnerUserID,
		OwnerName:  owner.AccountID,
		OwnerBio:   owner.Bio,
		CreatedAt:  room.CreatedAt,
	}, nil
}
