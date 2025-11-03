package usecase

import (
	"context"
	_interface "main/features/memolike/model/interface"
	"time"
)

type ToggleMemoLikeUseCase struct {
	Repository     _interface.IToggleMemoLikeRepository
	ContextTimeout time.Duration
}

func NewToggleMemoLikeUseCase(repo _interface.IToggleMemoLikeRepository, timeout time.Duration) _interface.IToggleMemoLikeUseCase {
	return &ToggleMemoLikeUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// Execute 메모 좋아요 토글 (추가 또는 제거)
// 반환값: isLiked (true=좋아요 추가됨, false=좋아요 제거됨), error
func (uc *ToggleMemoLikeUseCase) Execute(ctx context.Context, memoID uint, userID uint) (bool, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// 1. 좋아요 존재 여부 확인
	exists, err := uc.Repository.CheckMemoLikeExists(ctx, memoID, userID)
	if err != nil {
		return false, err
	}

	// 2. 존재하면 제거, 없으면 추가
	if exists {
		// 좋아요 제거
		if err := uc.Repository.DeleteMemoLike(ctx, memoID, userID); err != nil {
			return false, err
		}

		// 좋아요 개수 감소
		if err := uc.Repository.DecrementMemoLikesCount(ctx, memoID); err != nil {
			return false, err
		}

		return false, nil // 좋아요 제거됨
	} else {
		// 좋아요 추가
		if err := uc.Repository.CreateMemoLike(ctx, memoID, userID); err != nil {
			return false, err
		}

		// 좋아요 개수 증가
		if err := uc.Repository.IncrementMemoLikesCount(ctx, memoID); err != nil {
			return false, err
		}

		return true, nil // 좋아요 추가됨
	}
}
