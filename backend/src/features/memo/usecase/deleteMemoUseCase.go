package usecase

import (
	"context"
	_interface "main/features/memo/model/interface"
	"time"
)

type DeleteMemoUseCase struct {
	Repository     _interface.IDeleteMemoRepository
	ContextTimeout time.Duration
}

func NewDeleteMemoUseCase(repo _interface.IDeleteMemoRepository, timeout time.Duration) _interface.IDeleteMemoUseCase {
	return &DeleteMemoUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// DeleteMemo 메모 삭제
func (uc *DeleteMemoUseCase) DeleteMemo(ctx context.Context, memoID uint, userID uint) error {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	return uc.Repository.Delete(ctx, memoID, userID)
}
