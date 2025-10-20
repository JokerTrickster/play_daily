package usecase

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/request"
	"main/features/memo/model/response"
	"time"
)

type CreateMemoUseCase struct {
	Repository     _interface.ICreateMemoRepository
	ContextTimeout time.Duration
}

func NewCreateMemoUseCase(repo _interface.ICreateMemoRepository, timeout time.Duration) _interface.ICreateMemoUseCase {
	return &CreateMemoUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// CreateMemo 메모 생성
func (uc *CreateMemoUseCase) CreateMemo(ctx context.Context, userID uint, req request.ReqCreateMemo) (*response.ResMemo, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	memo := &mysql.Memo{
		UserID:       userID,
		Title:        req.Title,
		Content:      req.Content,
		ImageURL:     req.ImageURL,
		Rating:       req.Rating,
		IsPinned:     req.IsPinned,
		Latitude:     req.Latitude,
		Longitude:    req.Longitude,
		LocationName: req.LocationName,
		Category:     req.Category,
	}

	err := uc.Repository.Create(ctx, memo)
	if err != nil {
		return nil, err
	}

	return convertMemoToResponse(memo), nil
}
