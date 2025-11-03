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

	// 2. Verify room password
	isValid, err := uc.Repository.VerifyRoomPassword(ctx, req.RoomID, req.RoomPassword)
	if err != nil {
		return nil, err
	}
	if !isValid {
		return nil, errors.New("invalid room password")
	}

	// 3. Update user's default room
	if err := uc.Repository.UpdateUserDefaultRoom(ctx, userID, req.RoomID); err != nil {
		return nil, err
	}

	return room, nil
}
