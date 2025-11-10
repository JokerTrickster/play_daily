package usecase

import (
	"context"
	"main/features/room/model/request"
	"time"
)

type UpdateRoomUseCase struct {
	repo    UpdateRoomRepositoryInterface
	timeout time.Duration
}

type UpdateRoomRepositoryInterface interface {
	UpdateRoomSettings(ctx context.Context, roomID int64, isPublic *bool, roomPassword string) error
	IsOwner(ctx context.Context, roomID, userID int64) (bool, error)
}

func NewUpdateRoomUseCase(repo UpdateRoomRepositoryInterface, timeout time.Duration) *UpdateRoomUseCase {
	return &UpdateRoomUseCase{
		repo:    repo,
		timeout: timeout,
	}
}

func (uc *UpdateRoomUseCase) Execute(ctx context.Context, requesterUserID int64, req *request.UpdateRoomRequest) error {
	ctx, cancel := context.WithTimeout(ctx, uc.timeout)
	defer cancel()

	// 1. 요청자가 방장인지 확인
	isOwner, err := uc.repo.IsOwner(ctx, req.RoomID, requesterUserID)
	if err != nil {
		return err
	}

	if !isOwner {
		return &UseCaseError{Code: "FORBIDDEN", Message: "Only room owner can update room settings"}
	}

	// 2. 방 설정 업데이트
	return uc.repo.UpdateRoomSettings(ctx, req.RoomID, req.IsPublic, req.RoomPassword)
}
