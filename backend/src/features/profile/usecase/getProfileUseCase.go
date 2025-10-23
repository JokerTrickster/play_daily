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

	return &response.ResProfile{
		UserID:          user.ID,
		AccountID:       user.AccountID,
		Nickname:        user.Nickname,
		ProfileImageURL: user.ProfileImageURL,
		DefaultRoomID:   user.DefaultRoomID,
	}, nil
}
