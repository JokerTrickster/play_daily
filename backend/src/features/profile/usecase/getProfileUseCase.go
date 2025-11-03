package usecase

import (
	"context"
	_interface "main/features/profile/model/interface"
	"main/features/profile/model/response"
	"time"
)

type GetProfileUseCase struct {
	Repository     _interface.IGetProfileRepository
	ContextTimeout time.Duration
}

func NewGetProfileUseCase(repo _interface.IGetProfileRepository, timeout time.Duration) _interface.IGetProfileUseCase {
	return &GetProfileUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

func (uc *GetProfileUseCase) GetProfile(ctx context.Context, userID uint) (*response.ResProfile, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	user, err := uc.Repository.GetByUserID(ctx, userID)
	if err != nil {
		return nil, err
	}

	// 내 방이 받은 좋아요 개수 가져오기
	var receivedLikesCount uint
	if user.DefaultRoom != nil {
		receivedLikesCount = user.DefaultRoom.LikesCount
	}

	return &response.ResProfile{
		UserID:             user.ID,
		AccountID:          user.AccountID,
		Nickname:           user.Nickname,
		ProfileImageURL:    user.ProfileImageURL,
		DefaultRoomID:      user.DefaultRoomID,
		RoomPassword:       user.RoomPassword,
		ReceivedLikesCount: receivedLikesCount,
	}, nil
}
