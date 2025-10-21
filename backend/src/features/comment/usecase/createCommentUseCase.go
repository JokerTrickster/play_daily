package usecase

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/comment/model/interface"
	"main/features/comment/model/request"
	"main/features/comment/model/response"
)

type CreateCommentUseCase struct {
	CreateCommentRepository _interface.ICreateCommentRepository
}

func NewCreateCommentUseCase(repo _interface.ICreateCommentRepository) _interface.ICreateCommentUseCase {
	return &CreateCommentUseCase{
		CreateCommentRepository: repo,
	}
}

func (u *CreateCommentUseCase) Execute(ctx context.Context, memoID uint, userID uint, req *request.ReqCreateComment) (*response.ResComment, error) {
	comment := &mysql.Comment{
		MemoID:  memoID,
		UserID:  userID,
		Content: req.Content,
		Rating:  req.Rating,
	}

	if err := u.CreateCommentRepository.Create(ctx, comment); err != nil {
		return nil, err
	}

	return &response.ResComment{
		ID:        comment.ID,
		MemoID:    comment.MemoID,
		UserID:    comment.UserID,
		UserName:  "현재 사용자", // TODO: 실제 사용자 이름으로 교체
		Content:   comment.Content,
		Rating:    comment.Rating,
		CreatedAt: comment.CreatedAt,
		UpdatedAt: comment.UpdatedAt,
	}, nil
}
