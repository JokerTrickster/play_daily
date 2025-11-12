package _interface

import (
	"context"
	"main/features/auth/model/request"
	"main/features/auth/model/response"
)

type IReissueAuthUseCase interface {
	Reissue(ctx context.Context, req request.ReqReissue) (*response.ResReissue, error)
}
