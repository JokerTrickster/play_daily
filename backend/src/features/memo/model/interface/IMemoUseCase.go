package _interface

import (
	"context"
	"main/features/memo/model/request"
	"main/features/memo/model/response"
)

type ICreateMemoUseCase interface {
	CreateMemo(ctx context.Context, userID uint, req request.ReqCreateMemo) (*response.ResMemo, error)
}

type IGetMemoUseCase interface {
	GetMemo(ctx context.Context, memoID uint, userID uint) (*response.ResMemo, error)
	GetMemoList(ctx context.Context, userID uint, isWishlist *bool) (*response.ResMemoList, error)
}

type IUpdateMemoUseCase interface {
	UpdateMemo(ctx context.Context, memoID uint, userID uint, req request.ReqUpdateMemo) (*response.ResMemo, error)
}

type IDeleteMemoUseCase interface {
	DeleteMemo(ctx context.Context, memoID uint, userID uint) error
}
