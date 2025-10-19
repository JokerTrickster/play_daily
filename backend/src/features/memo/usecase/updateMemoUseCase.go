package usecase

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/request"
	"main/features/memo/model/response"

	"time"
)

type UpdateMemoUseCase struct {
	Repository     _interface.IUpdateMemoRepository
	ContextTimeout time.Duration
}

func NewUpdateMemoUseCase(repo _interface.IUpdateMemoRepository, timeout time.Duration) _interface.IUpdateMemoUseCase {
	return &UpdateMemoUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// UpdateMemo 메모 수정
func (uc *UpdateMemoUseCase) UpdateMemo(ctx context.Context, memoID uint, userID uint, req request.ReqUpdateMemo) (*response.ResMemo, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	updateMemo := &mysql.Memo{
		Title:        req.Title,
		Content:      req.Content,
		ImageURL:     req.ImageURL,
		Rating:       req.Rating,
		IsPinned:     req.IsPinned,
		Latitude:     req.Latitude,
		Longitude:    req.Longitude,
		LocationName: req.LocationName,
	}

	err := uc.Repository.Update(ctx, memoID, userID, updateMemo)
	if err != nil {
		return nil, err
	}

	// 업데이트된 메모 조회
	updatedMemo, err := uc.Repository.GetByID(ctx, memoID, userID)
	if err != nil {
		return nil, err
	}

	return convertMemoToResponse(updatedMemo), nil
}
