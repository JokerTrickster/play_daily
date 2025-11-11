package usecase

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"
	"main/features/room/model/response"
	"time"
)

type RoomDiscoveryUseCase struct {
	Repository     _interface.IRoomDiscoveryRepository
	ContextTimeout time.Duration
}

func NewRoomDiscoveryUseCase(repo _interface.IRoomDiscoveryRepository, timeout time.Duration) _interface.IRoomDiscoveryUseCase {
	return &RoomDiscoveryUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// GetPopularRooms retrieves popular rooms with pagination
func (uc *RoomDiscoveryUseCase) GetPopularRooms(ctx context.Context, page int, limit int) (*response.ResRoomList, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// Calculate offset
	offset := (page - 1) * limit

	// Get total count
	total, err := uc.Repository.CountPublicRooms(ctx)
	if err != nil {
		return nil, err
	}

	// Get rooms
	rooms, err := uc.Repository.GetPopularRooms(ctx, offset, limit)
	if err != nil {
		return nil, err
	}

	// Calculate hasNext
	hasNext := int64(offset+limit) < total

	// Convert to response
	return convertToRoomListResponse(rooms, total, page, limit, hasNext), nil
}

// GetRecentRooms retrieves recent rooms with pagination
func (uc *RoomDiscoveryUseCase) GetRecentRooms(ctx context.Context, page int, limit int) (*response.ResRoomList, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// Calculate offset
	offset := (page - 1) * limit

	// Get total count
	total, err := uc.Repository.CountPublicRooms(ctx)
	if err != nil {
		return nil, err
	}

	// Get rooms
	rooms, err := uc.Repository.GetRecentRooms(ctx, offset, limit)
	if err != nil {
		return nil, err
	}

	// Calculate hasNext
	hasNext := int64(offset+limit) < total

	// Convert to response
	return convertToRoomListResponse(rooms, total, page, limit, hasNext), nil
}

// convertToRoomListResponse converts mysql.Room slice to response.ResRoomList
func convertToRoomListResponse(rooms []mysql.Room, total int64, page int, limit int, hasNext bool) *response.ResRoomList {
	resRooms := make([]response.ResRoomListItem, len(rooms))
	for i, room := range rooms {
		resRooms[i] = response.ResRoomListItem{
			ID:         room.ID,
			Name:       room.Name,
			RoomCode:   room.RoomCode,
			IsPublic:   room.IsPublic,
			LikesCount: room.LikesCount,
			OwnerID:    room.OwnerUserID,
			CreatedAt:  room.CreatedAt,
		}
	}

	return &response.ResRoomList{
		Rooms:   resRooms,
		Total:   total,
		Page:    page,
		Limit:   limit,
		HasNext: hasNext,
	}
}
