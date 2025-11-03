package _interface

import (
	"context"

	"github.com/labstack/echo/v4"
)

// IToggleMemoLikeUseCase 메모 좋아요 토글 유스케이스 인터페이스
type IToggleMemoLikeUseCase interface {
	Execute(ctx context.Context, memoID uint, userID uint) (isLiked bool, err error)
}

// IToggleMemoLikeRepository 메모 좋아요 토글 레포지토리 인터페이스
type IToggleMemoLikeRepository interface {
	CheckMemoLikeExists(ctx context.Context, memoID uint, userID uint) (bool, error)
	CreateMemoLike(ctx context.Context, memoID uint, userID uint) error
	DeleteMemoLike(ctx context.Context, memoID uint, userID uint) error
	IncrementMemoLikesCount(ctx context.Context, memoID uint) error
	DecrementMemoLikesCount(ctx context.Context, memoID uint) error
}

// IToggleMemoLikeHandler 메모 좋아요 토글 핸들러 인터페이스
type IToggleMemoLikeHandler interface {
	ToggleLike(c echo.Context) error
}
