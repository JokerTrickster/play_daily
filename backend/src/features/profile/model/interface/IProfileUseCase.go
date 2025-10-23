package _interface

import (
	"context"
	"main/features/profile/model/response"
)

type IGetProfileUseCase interface {
	GetProfile(ctx context.Context, userID uint) (*response.ResProfile, error)
}
