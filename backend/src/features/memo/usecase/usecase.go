package usecase

import (
	"main/common/db/mysql"
	"main/features/memo/model/response"
)

// convertMemoToResponse mysql.Memo를 response.ResMemo로 변환
func convertMemoToResponse(memo *mysql.Memo) *response.ResMemo {
	return &response.ResMemo{
		ID:           memo.ID,
		UserID:       memo.UserID,
		Title:        memo.Title,
		Content:      memo.Content,
		ImageURL:     memo.ImageURL,
		Rating:       memo.Rating,
		IsPinned:     memo.IsPinned,
		Latitude:     memo.Latitude,
		Longitude:    memo.Longitude,
		LocationName: memo.LocationName,
		CreatedAt:    memo.CreatedAt,
		UpdatedAt:    memo.UpdatedAt,
	}
}
