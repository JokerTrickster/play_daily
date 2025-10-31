package usecase

import (
	"context"
	_interface "main/features/roomlike/model/interface"
)

type UnlikeRoomUseCase struct {
	Repository _interface.IRoomLikeRepository
}

func NewUnlikeRoomUseCase(repository _interface.IRoomLikeRepository) _interface.IUnlikeRoomUseCase {
	return &UnlikeRoomUseCase{
		Repository: repository,
	}
}

func (u *UnlikeRoomUseCase) Execute(ctx context.Context, roomID, userID uint) error {
	return u.Repository.DeleteRoomLike(ctx, roomID, userID)
}
