package _interface

import (
	"context"
	"main/common/db/mysql"
)

type ICreateMemoRepository interface {
	Create(ctx context.Context, memo *mysql.Memo) error
}

type IGetMemoRepository interface {
	GetByID(ctx context.Context, id uint, userID uint) (*mysql.Memo, error)
	GetListByUserID(ctx context.Context, userID uint) ([]mysql.Memo, error)
	GetListWithFilters(ctx context.Context, userID uint, roomID *uint, isWishlist *bool) ([]mysql.Memo, error)
}

type IUpdateMemoRepository interface {
	Update(ctx context.Context, id uint, userID uint, memo *mysql.Memo) error
	GetByID(ctx context.Context, id uint, userID uint) (*mysql.Memo, error)
}

type IDeleteMemoRepository interface {
	Delete(ctx context.Context, id uint, userID uint) error
}
