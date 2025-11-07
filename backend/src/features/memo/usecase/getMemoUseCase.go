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

	// 사용자가 이 메모를 좋아요 했는지 확인
	isLiked, err := uc.Repository.CheckUserLikedMemo(ctx, memoID, userID)
	if err != nil {
		return nil, err
	}

	return convertMemoToResponse(memo, isLiked), nil
}

// GetMemoList 메모 목록 조회 (Room ID, 위시리스트 필터, 카테고리 필터, 페이지네이션 옵션 포함)
func (uc *GetMemoUseCase) GetMemoList(ctx context.Context, userID uint, roomID *uint, isWishlist *bool, categoryIDs []uint, page int, limit int) (*response.ResMemoList, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// 페이지네이션 계산
	offset := (page - 1) * limit

	// 전체 카운트 조회
	total, err := uc.Repository.CountByUserID(ctx, userID, roomID, isWishlist, categoryIDs)
	if err != nil {
		return nil, err
	}

	// 메모 목록 조회 (페이지네이션 적용)
	memos, err := uc.Repository.GetListByUserID(ctx, userID, roomID, isWishlist, categoryIDs, offset, limit)
	if err != nil {
		return nil, err
	}

	// 모든 메모 ID 수집
	memoIDs := make([]uint, len(memos))
	for i, memo := range memos {
		memoIDs[i] = memo.ID
	}

	// 배치로 좋아요 상태 확인 (N+1 query 해결)
	likedMap, err := uc.Repository.CheckUserLikedMemos(ctx, memoIDs, userID)
	if err != nil {
		return nil, err
	}

	// 응답 변환
	resMemos := make([]response.ResMemo, len(memos))
	for i, memo := range memos {
		isLiked := likedMap[memo.ID]
		resMemos[i] = *convertMemoToResponse(&memo, isLiked)
	}

	return &response.ResMemoList{
		Memos: resMemos,
		Total: total,
		Page:  page,
		Limit: limit,
	}, nil
}
