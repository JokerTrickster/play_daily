package usecase

import (
	"context"
	"errors"
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"main/features/room/model/response"
	"main/common/utils"
	"time"
)

type UpdateRoomPrivacyUseCase struct {
	UpdateRoomPrivacyRepository _interface.IUpdateRoomPrivacyRepository
	CtxTimeout                  time.Duration
}

func NewUpdateRoomPrivacyUseCase(repo _interface.IUpdateRoomPrivacyRepository, timeout time.Duration) _interface.IUpdateRoomPrivacyUseCase {
	return &UpdateRoomPrivacyUseCase{
		UpdateRoomPrivacyRepository: repo,
		CtxTimeout:                  timeout,
	}
}

// UpdateRoomPrivacy updates the room's privacy settings
func (u *UpdateRoomPrivacyUseCase) UpdateRoomPrivacy(ctx context.Context, roomID uint, userID uint, req *request.ReqUpdateRoomPrivacy) (*response.ResUpdateRoomPrivacy, error) {
	ctx, cancel := context.WithTimeout(ctx, u.CtxTimeout)
	defer cancel()

	// 1. Find room
	room, err := u.UpdateRoomPrivacyRepository.GetRoomByID(ctx, roomID)
	if err != nil {
		return nil, err
	}

	// 2. Validate ownership
	if room.OwnerUserID != userID {
		return nil, errors.New("not room owner")
	}

	// 3. Update privacy settings
	room.IsPublic = req.IsPublic
	if !req.IsPublic {
		// Generate password for private room
		password := utils.GenerateRoomPassword()
		room.Password = &password
	} else {
		// Clear password for public room
		room.Password = nil
	}

	// 4. Save atomically
	if err := u.UpdateRoomPrivacyRepository.UpdateRoom(ctx, room); err != nil {
		return nil, err
	}

	// 5. Return response
	return &response.ResUpdateRoomPrivacy{
		ID:       room.ID,
		RoomCode: room.RoomCode,
		IsPublic: room.IsPublic,
		Password: room.Password,
	}, nil
}
