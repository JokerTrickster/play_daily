package _interface

import (
	"context"
	"main/features/profile/model/request"
	"main/features/profile/model/response"
)

type IGetProfileUseCase interface {
	GetProfile(ctx context.Context, userID uint) (*response.ResProfile, error)
}

type IUpdateProfileUseCase interface {
	UpdateProfile(ctx context.Context, userID uint, req request.ReqUpdateProfile) (*response.ResProfile, error)
}
