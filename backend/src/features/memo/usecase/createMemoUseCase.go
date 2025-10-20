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

type CreateMemoUseCase struct {
	Repository     _interface.ICreateMemoRepository
	ContextTimeout time.Duration
}

func NewCreateMemoUseCase(repo _interface.ICreateMemoRepository, timeout time.Duration) _interface.ICreateMemoUseCase {
	return &CreateMemoUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// CreateMemo 메모 생성
func (uc *CreateMemoUseCase) CreateMemo(ctx context.Context, userID uint, req request.ReqCreateMemo) (*response.ResMemo, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// 이미지 파일이 있으면 S3에 업로드
	imageURL := req.ImageURL
	if req.ImageFile != nil && req.ImageHeader != nil {
		if storage.S3 == nil {
			return nil, fmt.Errorf("S3 storage is not configured")
		}

		uploadedURL, err := storage.S3.UploadFile(ctx, req.ImageFile, req.ImageHeader, "image/daily")
		if err != nil {
			return nil, fmt.Errorf("failed to upload image to S3: %w", err)
		}
		imageURL = uploadedURL
	}

	memo := &mysql.Memo{
		UserID:       userID,
		Title:        req.Title,
		Content:      req.Content,
		ImageURL:     imageURL,
		Rating:       req.Rating,
		IsPinned:     req.IsPinned,
		Latitude:     req.Latitude,
		Longitude:    req.Longitude,
		LocationName: req.LocationName,
		Category:     req.Category,
	}

	err := uc.Repository.Create(ctx, memo)
	if err != nil {
		return nil, err
	}

	return convertMemoToResponse(memo), nil
}
