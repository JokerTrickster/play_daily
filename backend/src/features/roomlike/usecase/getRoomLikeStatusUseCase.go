package usecase

import (
	"context"
	_interface "main/features/roomlike/model/interface"
	"main/features/roomlike/model/response"
)

type GetRoomLikeStatusUseCase struct {
	Repository _interface.IRoomLikeRepository
}

func NewGetRoomLikeStatusUseCase(repository _interface.IRoomLikeRepository) _interface.IGetRoomLikeStatusUseCase {
	return &GetRoomLikeStatusUseCase{
		Repository: repository,
	}
}

func (u *GetRoomLikeStatusUseCase) Execute(ctx context.Context, roomID, userID uint) (*response.ResRoomLikeStatus, error) {
	return u.Repository.CheckRoomLikeStatus(ctx, roomID, userID)
}
