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

// CreateMemo 메모 생성 (카테고리 포함)
func (uc *CreateMemoUseCase) CreateMemo(ctx context.Context, userID uint, req request.ReqCreateMemo) (*response.ResMemo, error) {
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// 카테고리 검증 포함 전체 요청 검증 (DB 접근 필요)
	// Note: 실제로는 gorm DB 인스턴스를 전달해야 하지만, 임시로 repository를 통해 검증
	// 이후 handler에서 검증하도록 수정 가능

	// 비즈니스 정보 필드 검증
	if err := request.ValidateBusinessFields(req.BusinessName, req.BusinessPhone, req.BusinessAddress); err != nil {
		return nil, err
	}

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
		UserID:          userID,
		RoomID:          req.RoomID,
		Title:           req.Title,
		// Content field removed - using categories instead
		CreationMode:    req.CreationMode, // NEW
		ImageURL:        imageURL,
		Rating:          req.Rating,
		IsPinned:        req.IsPinned,
		Latitude:        req.Latitude,
		Longitude:       req.Longitude,
		LocationName:    req.LocationName,
		Category:        req.Category, // Deprecated
		IsWishlist:      req.IsWishlist,
		BusinessName:    req.BusinessName,
		BusinessPhone:   req.BusinessPhone,
		BusinessAddress: req.BusinessAddress,
		NaverPlaceURL:   req.NaverPlaceURL,
	}

	// 카테고리와 함께 트랜잭션으로 생성
	err := uc.Repository.CreateWithCategories(ctx, memo, req.CategoryIDs)
	if err != nil {
		return nil, err
	}

	// 생성 시점에는 좋아요가 없으므로 false
	return convertMemoToResponse(memo, false), nil
}
