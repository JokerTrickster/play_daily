package usecase

import (
	"context"
	_interface "main/features/roomlike/model/interface"
)

type LikeRoomUseCase struct {
	Repository _interface.IRoomLikeRepository
}

func NewLikeRoomUseCase(repository _interface.IRoomLikeRepository) _interface.ILikeRoomUseCase {
	return &LikeRoomUseCase{
		Repository: repository,
	}
}

func (u *LikeRoomUseCase) Execute(ctx context.Context, roomID, userID uint) error {
	return u.Repository.CreateRoomLike(ctx, roomID, userID)
}
