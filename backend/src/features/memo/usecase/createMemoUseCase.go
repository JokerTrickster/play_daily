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

	// 1. 권한 체크: 사용자가 해당 방에 쓰기 권한이 있는지 확인
	hasPermission, err := uc.Repository.CheckWritePermission(ctx, req.RoomID, userID)
	if err != nil {
		return nil, fmt.Errorf("failed to check write permission: %w", err)
	}
	if !hasPermission {
		return nil, fmt.Errorf("no write permission for this room")
	}

	// 2. 요청 검증은 이미 handler에서 ValidateCreateMemo로 수행됨
	// 중복 검증 제거

	// 이미지 URL 초기화 (파일 업로드는 트랜잭션 성공 후 처리)
	imageURL := req.ImageURL

	// 메모 객체 생성 (이미지 URL은 임시 값)
	memo := &mysql.Memo{
		UserID:       userID,
		RoomID:       req.RoomID,
		Title:        req.Title,
		// Content and Category fields removed - using categories instead
		CreationMode:    req.CreationMode, // NEW
		ImageURL:        imageURL,
		Rating:          req.Rating,
		IsPinned:        req.IsPinned,
		Latitude:        req.Latitude,
		Longitude:       req.Longitude,
		LocationName:    req.LocationName,
		IsWishlist:      req.IsWishlist,
		BusinessName:    req.BusinessName,
		BusinessPhone:   req.BusinessPhone,
		BusinessAddress: req.BusinessAddress,
		NaverPlaceURL:   req.NaverPlaceURL,
	}

	// 카테고리와 함께 트랜잭션으로 생성
	err = uc.Repository.CreateWithCategories(ctx, memo, req.CategoryIDs)
	if err != nil {
		return nil, err
	}

	// 트랜잭션 성공 후 이미지 파일이 있으면 S3에 업로드하고 메모 업데이트
	if req.ImageFile != nil && req.ImageHeader != nil {
		if storage.S3 == nil {
			return nil, fmt.Errorf("S3 storage is not configured")
		}

		uploadedURL, err := storage.S3.UploadFile(ctx, req.ImageFile, req.ImageHeader, "image/daily")
		if err != nil {
			// S3 업로드 실패 시 로그만 남기고 메모는 이미지 없이 유지
			// 또는 생성된 메모를 롤백하려면 삭제 처리
			// 여기서는 로그만 남기고 진행
			fmt.Printf("Warning: failed to upload image to S3 for memo %d: %v\n", memo.ID, err)
		} else {
			// S3 업로드 성공 시 메모의 이미지 URL 업데이트
			memo.ImageURL = uploadedURL
			// 이미지 URL만 업데이트
			if updateErr := uc.Repository.UpdateImageURL(ctx, memo.ID, uploadedURL); updateErr != nil {
				fmt.Printf("Warning: failed to update image URL for memo %d: %v\n", memo.ID, updateErr)
			}
		}
	}

	// 생성 시점에는 좋아요가 없으므로 false
	return convertMemoToResponse(memo, false), nil
}
