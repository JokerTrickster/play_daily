package usecase

import (
	"context"
	"errors"
	_interface "main/features/room/model/interface"
	"main/features/room/model/response"
	"time"
)

type GetRoomMembersUseCase struct {
	GetRoomMembersRepository _interface.IGetRoomMembersRepository
	CtxTimeout               time.Duration
}

func NewGetRoomMembersUseCase(repo _interface.IGetRoomMembersRepository, timeout time.Duration) _interface.IGetRoomMembersUseCase {
	return &GetRoomMembersUseCase{
		GetRoomMembersRepository: repo,
		CtxTimeout:               timeout,
	}
}

// GetRoomMembers 방 멤버 목록 조회
func (u *GetRoomMembersUseCase) GetRoomMembers(ctx context.Context, userID uint, roomID uint) (*response.ResRoomMembers, error) {
	ctx, cancel := context.WithTimeout(ctx, u.CtxTimeout)
	defer cancel()

	// 1. 요청자가 방 멤버인지 확인
	isMember, err := u.GetRoomMembersRepository.CheckRoomMember(ctx, roomID, userID)
	if err != nil {
		return nil, err
	}

	if !isMember {
		return nil, errors.New("방 멤버만 멤버 목록을 조회할 수 있습니다")
	}

	// 2. 방 멤버 목록 조회
	members, err := u.GetRoomMembersRepository.GetRoomMembers(ctx, roomID)
	if err != nil {
		return nil, errors.New("멤버 목록 조회에 실패했습니다")
	}

	return members, nil
}
