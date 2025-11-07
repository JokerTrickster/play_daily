package _interface

import (
	"context"
	"main/common/db/mysql"
)

type ICreateMemoRepository interface {
	Create(ctx context.Context, memo *mysql.Memo) error
	CreateWithCategories(ctx context.Context, memo *mysql.Memo, categoryIDs []uint) error
}

type IGetMemoRepository interface {
	GetByID(ctx context.Context, id uint, userID uint) (*mysql.Memo, error)
	GetListByUserID(ctx context.Context, userID uint, roomID *uint, isWishlist *bool, categoryIDs []uint, offset int, limit int) ([]mysql.Memo, error)
	CountByUserID(ctx context.Context, userID uint, roomID *uint, isWishlist *bool, categoryIDs []uint) (int64, error)
	CheckUserLikedMemo(ctx context.Context, memoID uint, userID uint) (bool, error)
	CheckUserLikedMemos(ctx context.Context, memoIDs []uint, userID uint) (map[uint]bool, error) // NEW: Batch like check
}

type IUpdateMemoRepository interface {
	Update(ctx context.Context, id uint, userID uint, memo *mysql.Memo) error
	GetByID(ctx context.Context, id uint, userID uint) (*mysql.Memo, error)
	CheckUserLikedMemo(ctx context.Context, memoID uint, userID uint) (bool, error)
}

type IDeleteMemoRepository interface {
	Delete(ctx context.Context, id uint, userID uint) error
}
