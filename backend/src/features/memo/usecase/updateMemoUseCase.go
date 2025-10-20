package usecase

import (
	"context"
	"fmt"
	"main/common/db/mysql"
	"main/common/storage"
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/request"
	"main/features/memo/model/response"

	"time"
)

type UpdateMemoUseCase struct {
	Repository     _interface.IUpdateMemoRepository
	ContextTimeout time.Duration
}

func NewUpdateMemoUseCase(repo _interface.IUpdateMemoRepository, timeout time.Duration) _interface.IUpdateMemoUseCase {
	return &UpdateMemoUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// UpdateMemo 메모 수정
func (uc *UpdateMemoUseCase) UpdateMemo(ctx context.Context, memoID uint, userID uint, req request.ReqUpdateMemo) (*response.ResMemo, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// 이미지 파일이 있으면 S3에 업로드
	imageURL := req.ImageURL
	if req.ImageFile != nil && req.ImageHeader != nil {
		if storage.S3 == nil {
			return nil, fmt.Errorf("S3 storage is not configured")
		}

		// 기존 이미지가 있으면 삭제 (선택사항)
		// oldMemo, _ := uc.Repository.GetByID(ctx, memoID, userID)
		// if oldMemo != nil && oldMemo.ImageURL != "" {
		//     storage.S3.DeleteFile(ctx, oldMemo.ImageURL)
		// }

		uploadedURL, err := storage.S3.UploadFile(ctx, req.ImageFile, req.ImageHeader, "image/daily")
		if err != nil {
			return nil, fmt.Errorf("failed to upload image to S3: %w", err)
		}
		imageURL = uploadedURL
	}

	updateMemo := &mysql.Memo{
		Title:        req.Title,
		Content:      req.Content,
		ImageURL:     imageURL,
		Rating:       req.Rating,
		IsPinned:     req.IsPinned,
		Latitude:     req.Latitude,
		Longitude:    req.Longitude,
		LocationName: req.LocationName,
	}

	err := uc.Repository.Update(ctx, memoID, userID, updateMemo)
	if err != nil {
		return nil, err
	}

	// 업데이트된 메모 조회
	updatedMemo, err := uc.Repository.GetByID(ctx, memoID, userID)
	if err != nil {
		return nil, err
	}

	return convertMemoToResponse(updatedMemo), nil
}
