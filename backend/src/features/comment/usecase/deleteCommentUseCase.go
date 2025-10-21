package usecase

import (
	"context"
	_interface "main/features/comment/model/interface"
)

type DeleteCommentUseCase struct {
	DeleteCommentRepository _interface.IDeleteCommentRepository
}

func NewDeleteCommentUseCase(repo _interface.IDeleteCommentRepository) _interface.IDeleteCommentUseCase {
	return &DeleteCommentUseCase{
		DeleteCommentRepository: repo,
	}
}

func (u *DeleteCommentUseCase) Execute(ctx context.Context, commentID uint, userID uint) error {
	return u.DeleteCommentRepository.Delete(ctx, commentID, userID)
}
