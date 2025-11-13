package usecase

import (
	"context"
	"time"

	"main/features/room/model/response"
)

type GetRoomDetailUseCase struct {
	repo    GetRoomDetailRepositoryInterface
	timeout time.Duration
}

type GetRoomDetailRepositoryInterface interface {
	GetRoomDetail(ctx context.Context, roomID uint) (*response.ResRoomDetail, error)
}

func NewGetRoomDetailUseCase(repo GetRoomDetailRepositoryInterface, timeout time.Duration) *GetRoomDetailUseCase {
	return &GetRoomDetailUseCase{
		repo:    repo,
		timeout: timeout,
	}
}

func (uc *GetRoomDetailUseCase) Execute(ctx context.Context, roomID uint) (*response.ResRoomDetail, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.timeout)
	defer cancel()

	return uc.repo.GetRoomDetail(ctx, roomID)
}
