package usecase

import (
	"context"
	"errors"
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"main/features/room/model/response"
	"time"
)

type JoinRoomUseCase struct {
	Repository     _interface.IJoinRoomRepository
	ContextTimeout time.Duration
}

func NewJoinRoomUseCase(repo _interface.IJoinRoomRepository, timeout time.Duration) _interface.IJoinRoomUseCase {
	return &JoinRoomUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

func (uc *JoinRoomUseCase) JoinRoom(ctx context.Context, userID uint, req *request.ReqJoinRoom) (*response.ResRoom, error) {
	_, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// 1. Check if room exists
	room, err := uc.Repository.GetRoomByID(ctx, req.RoomID)
	if err != nil {
		return nil, err
	}

	// 2. Check if user is already a member
	isMember, err := uc.Repository.CheckRoomMemberExists(ctx, req.RoomID, userID)
	if err != nil {
		return nil, err
	}

	// 3. If already a member, return success
	if isMember {
		return room, nil
	}

	// 4. Verify room password (only for new members)
	isValid, err := uc.Repository.VerifyRoomPassword(ctx, req.RoomID, req.RoomPassword)
	if err != nil {
		return nil, err
	}
	if !isValid {
		return nil, errors.New("invalid room password")
	}

	// 5. Add user to room_members with READ_ONLY permission
	err = uc.Repository.AddRoomMember(ctx, req.RoomID, userID)
	if err != nil {
		return nil, err
	}

	// 6. DO NOT update user's default_room_id
	// default_room_id should remain as the user's own room ID
	// The frontend will manage current_room_id separately in local storage

	return room, nil
}
