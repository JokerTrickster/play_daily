package usecase

import (
	"context"
	_interface "main/features/roomlike/model/interface"
	"main/features/roomlike/model/response"
)

type GetLikedRoomsUseCase struct {
	Repository _interface.IRoomLikeRepository
}

func NewGetLikedRoomsUseCase(repository _interface.IRoomLikeRepository) _interface.IGetLikedRoomsUseCase {
	return &GetLikedRoomsUseCase{
		Repository: repository,
	}
}

func (u *GetLikedRoomsUseCase) Execute(ctx context.Context, userID uint) ([]*response.ResLikedRoom, error) {
	return u.Repository.GetLikedRooms(ctx, userID)
}
