package usecase

import (
	"context"
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/response"
	"time"
)

type GetMemoUseCase struct {
	Repository     _interface.IGetMemoRepository
	ContextTimeout time.Duration
}

func NewGetMemoUseCase(repo _interface.IGetMemoRepository, timeout time.Duration) _interface.IGetMemoUseCase {
	return &GetMemoUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// GetMemo 특정 메모 조회
func (uc *GetMemoUseCase) GetMemo(ctx context.Context, memoID uint, userID uint) (*response.ResMemo, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	memo, err := uc.Repository.GetByID(ctx, memoID, userID)
	if err != nil {
		return nil, err
	}

	return convertMemoToResponse(memo), nil
}

// GetMemoList 메모 목록 조회 (위시리스트 필터 옵션 포함)
func (uc *GetMemoUseCase) GetMemoList(ctx context.Context, userID uint, isWishlist *bool) (*response.ResMemoList, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	memos, err := uc.Repository.GetListByUserID(ctx, userID, isWishlist)
	if err != nil {
		return nil, err
	}

	resMemos := make([]response.ResMemo, len(memos))
	for i, memo := range memos {
		resMemos[i] = *convertMemoToResponse(&memo)
	}

	return &response.ResMemoList{
		Memos: resMemos,
		Total: int64(len(resMemos)),
	}, nil
}
