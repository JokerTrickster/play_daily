package usecase

import (
	"context"
	_interface "main/features/profile/model/interface"
	"main/features/profile/model/request"
	"main/features/profile/model/response"
	"time"
)

type UpdateProfileUseCase struct {
	Repository     _interface.IUpdateProfileRepository
	ContextTimeout time.Duration
}

func NewUpdateProfileUseCase(repo _interface.IUpdateProfileRepository, timeout time.Duration) _interface.IUpdateProfileUseCase {
	return &UpdateProfileUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

func (uc *UpdateProfileUseCase) UpdateProfile(ctx context.Context, userID uint, req request.ReqUpdateProfile) (*response.ResProfile, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	user, err := uc.Repository.UpdateProfile(ctx, userID, req.CurrentPassword, req.Nickname, req.NewPassword, req.ProfileImageURL)
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
