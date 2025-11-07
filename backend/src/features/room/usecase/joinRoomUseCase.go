package usecase

import (
	"context"
	"errors"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"main/features/room/model/response"
	"time"
)

type JoinRoomUseCase struct {
	JoinRoomRepository _interface.IJoinRoomRepository
	CtxTimeout         time.Duration
}

func NewJoinRoomUseCase(repo _interface.IJoinRoomRepository, timeout time.Duration) _interface.IJoinRoomUseCase {
	return &JoinRoomUseCase{
		JoinRoomRepository: repo,
		CtxTimeout:         timeout,
	}
}

func (uc *JoinRoomUseCase) JoinRoom(ctx context.Context, userID uint, req *request.ReqJoinRoom) (*response.ResRoom, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.CtxTimeout)
	defer cancel()

	// 1. Find room by room code
	room, err := uc.JoinRoomRepository.GetRoomByRoomCode(ctx, req.RoomCode)
	if err != nil {
		return nil, err
	}

	// 2. Check existing membership
	existingMember, err := uc.JoinRoomRepository.FindExistingMember(ctx, room.ID, userID)
	if err != nil {
		return nil, err
	}

	isNewMember := false
	if existingMember != nil {
		// Already a member - return success (idempotent)
		memberCount, _ := uc.JoinRoomRepository.GetRoomMemberCount(ctx, room.ID)
		return &response.ResRoom{
			ID:          room.ID,
			RoomCode:    room.RoomCode,
			Name:        room.Name,
			IsPublic:    room.IsPublic,
			LikesCount:  room.LikesCount,
			MemberCount: uint(memberCount),
			Permission:  string(existingMember.Permission),
			IsNewMember: false,
		}, nil
	}

	// 3. Validate access based on privacy setting
	if !room.IsPublic {
		// Private room - require password
		if req.Password == nil || room.Password == nil {
			return nil, errors.New("invalid password")
		}
		if *req.Password != *room.Password {
			return nil, errors.New("invalid password")
		}
	}
	// Public rooms proceed without password check

	// 4. Add member with READ_ONLY permission
	member := &mysql.RoomMember{
		RoomID:     room.ID,
		UserID:     userID,
		Permission: mysql.PermissionReadOnly,
	}

	if err := uc.JoinRoomRepository.CreateRoomMember(ctx, member); err != nil {
		return nil, err
	}

	isNewMember = true

	// 5. Get member count
	memberCount, _ := uc.JoinRoomRepository.GetRoomMemberCount(ctx, room.ID)

	return &response.ResRoom{
		ID:          room.ID,
		RoomCode:    room.RoomCode,
		Name:        room.Name,
		IsPublic:    room.IsPublic,
		LikesCount:  room.LikesCount,
		MemberCount: uint(memberCount),
		Permission:  string(mysql.PermissionReadOnly),
		IsNewMember: isNewMember,
	}, nil
}
