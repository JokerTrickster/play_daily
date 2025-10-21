package _interface

import (
	"context"
	"main/common/db/mysql"
)

type ICreateCommentRepository interface {
	Create(ctx context.Context, comment *mysql.Comment) error
}

type IGetCommentRepository interface {
	GetListByMemoID(ctx context.Context, memoID uint) ([]mysql.Comment, error)
	GetByID(ctx context.Context, id uint) (*mysql.Comment, error)
}

type IDeleteCommentRepository interface {
	Delete(ctx context.Context, id uint, userID uint) error
}
