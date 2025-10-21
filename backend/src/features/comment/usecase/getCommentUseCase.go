package usecase

import (
	"context"
	_interface "main/features/comment/model/interface"
	"main/features/comment/model/response"
)

type GetCommentUseCase struct {
	GetCommentRepository _interface.IGetCommentRepository
}

func NewGetCommentUseCase(repo _interface.IGetCommentRepository) _interface.IGetCommentUseCase {
	return &GetCommentUseCase{
		GetCommentRepository: repo,
	}
}

func (u *GetCommentUseCase) ExecuteList(ctx context.Context, memoID uint) (*response.ResCommentList, error) {
	comments, err := u.GetCommentRepository.GetListByMemoID(ctx, memoID)
	if err != nil {
		return nil, err
	}

	resComments := make([]response.ResComment, len(comments))
	for i, comment := range comments {
		userName := "알 수 없음"
		if comment.User != nil {
			userName = comment.User.Nickname
			if userName == "" {
				userName = comment.User.AccountID
			}
		}

		resComments[i] = response.ResComment{
			ID:        comment.ID,
			MemoID:    comment.MemoID,
			UserID:    comment.UserID,
			UserName:  userName,
			Content:   comment.Content,
			Rating:    comment.Rating,
			CreatedAt: comment.CreatedAt,
			UpdatedAt: comment.UpdatedAt,
		}
	}

	return &response.ResCommentList{
		Comments: resComments,
		Total:    int64(len(resComments)),
	}, nil
}
