package _interface

import (
	"context"
	"main/features/comment/model/request"
	"main/features/comment/model/response"
)

type ICreateCommentUseCase interface {
	Execute(ctx context.Context, memoID uint, userID uint, req *request.ReqCreateComment) (*response.ResComment, error)
}

type IGetCommentUseCase interface {
	ExecuteList(ctx context.Context, memoID uint) (*response.ResCommentList, error)
}

type IDeleteCommentUseCase interface {
	Execute(ctx context.Context, commentID uint, userID uint) error
}
