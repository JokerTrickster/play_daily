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
	// 삭제 전에 메모 ID 조회
	memoID, err := u.DeleteCommentRepository.GetMemoIDByCommentID(ctx, commentID)
	if err != nil {
		return err
	}

	// 댓글 삭제
	if err := u.DeleteCommentRepository.Delete(ctx, commentID, userID); err != nil {
		return err
	}

	// 댓글 삭제 후 메모의 평점 업데이트
	if err := u.DeleteCommentRepository.UpdateMemoRating(ctx, memoID); err != nil {
		return err
	}

	return nil
}
