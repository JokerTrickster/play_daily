package usecase

import (
	"context"
	"errors"
	"main/features/room/model/request"
	_interface "main/features/room/model/interface"
	"time"
)

type KickUserUseCase struct {
	KickUserRepository _interface.IKickUserRepository
	CtxTimeout         time.Duration
}

func NewKickUserUseCase(kickUserRepo _interface.IKickUserRepository, timeout time.Duration) _interface.IKickUserUseCase {
	return &KickUserUseCase{
		KickUserRepository: kickUserRepo,
		CtxTimeout:         timeout,
	}
}

// KickUser 사용자 추방
func (u *KickUserUseCase) KickUser(ctx context.Context, ownerUserID uint, req *request.ReqKickUser) error {
	ctx, cancel := context.WithTimeout(ctx, u.CtxTimeout)
	defer cancel()

	// 1. 요청자가 방장인지 확인
	isOwner, err := u.KickUserRepository.CheckRoomOwner(ctx, req.RoomID, ownerUserID)
	if err != nil {
		return err
	}

	if !isOwner {
		return errors.New("방장만 사용자를 추방할 수 있습니다")
	}

	// 2. 방장 자신을 추방하려는지 확인
	if ownerUserID == req.TargetUserID {
		return errors.New("방장은 자신을 추방할 수 없습니다")
	}

	// 3. 사용자 추방 실행
	err = u.KickUserRepository.KickUserFromRoom(ctx, req.RoomID, req.TargetUserID)
	if err != nil {
		return errors.New("사용자 추방에 실패했습니다")
	}

	return nil
}
