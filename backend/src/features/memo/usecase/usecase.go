package usecase

import (
	"main/common/db/mysql"
	commentResponse "main/features/comment/model/response"
	"main/features/memo/model/response"
)

// convertMemoToResponse mysql.Memo를 response.ResMemo로 변환
func convertMemoToResponse(memo *mysql.Memo) *response.ResMemo {
	// 댓글 변환
	comments := make([]commentResponse.ResComment, len(memo.Comments))
	for i, comment := range memo.Comments {
		userName := "알 수 없음"
		if comment.User != nil {
			userName = comment.User.Nickname
			if userName == "" {
				userName = comment.User.AccountID
			}
		}

		comments[i] = commentResponse.ResComment{
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

	return &response.ResMemo{
		ID:              memo.ID,
		UserID:          memo.UserID,
		Title:           memo.Title,
		Content:         memo.Content,
		ImageURL:        memo.ImageURL,
		Rating:          memo.Rating,
		IsPinned:        memo.IsPinned,
		Latitude:        memo.Latitude,
		Longitude:       memo.Longitude,
		LocationName:    memo.LocationName,
		Category:        memo.Category,
		IsWishlist:      memo.IsWishlist,
		BusinessName:    memo.BusinessName,
		BusinessPhone:   memo.BusinessPhone,
		BusinessAddress: memo.BusinessAddress,
		NaverPlaceURL:   memo.NaverPlaceURL,
		Comments:        comments,
		CreatedAt:       memo.CreatedAt,
		UpdatedAt:       memo.UpdatedAt,
	}
}
