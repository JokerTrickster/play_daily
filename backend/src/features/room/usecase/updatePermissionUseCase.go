package usecase

import (
	"context"
	"main/features/room/model/request"
	"time"
)

type UpdatePermissionUseCase struct {
	repo    UpdatePermissionRepositoryInterface
	timeout time.Duration
}

type UpdatePermissionRepositoryInterface interface {
	UpdatePermission(ctx context.Context, roomID, userID int64, permission string) error
	IsOwner(ctx context.Context, roomID, userID int64) (bool, error)
}

func NewUpdatePermissionUseCase(repo UpdatePermissionRepositoryInterface, timeout time.Duration) *UpdatePermissionUseCase {
	return &UpdatePermissionUseCase{
		repo:    repo,
		timeout: timeout,
	}
}

func (uc *UpdatePermissionUseCase) Execute(ctx context.Context, requesterUserID int64, req *request.UpdatePermissionRequest) error {
	ctx, cancel := context.WithTimeout(ctx, uc.timeout)
	defer cancel()

	// 1. 요청자가 방장인지 확인
	isOwner, err := uc.repo.IsOwner(ctx, req.RoomID, requesterUserID)
	if err != nil {
		return err
	}

	if !isOwner {
		return ErrForbidden
	}

	// 2. 자기 자신의 권한은 변경할 수 없음
	if requesterUserID == req.UserID {
		return ErrCannotModifySelf
	}

	// 3. 권한 업데이트
	return uc.repo.UpdatePermission(ctx, req.RoomID, req.UserID, req.Permission)
}

var (
	ErrForbidden          = &UseCaseError{Code: "FORBIDDEN", Message: "Only room owner can update permissions"}
	ErrCannotModifySelf   = &UseCaseError{Code: "CANNOT_MODIFY_SELF", Message: "Cannot modify your own permission"}
)

type UseCaseError struct {
	Code    string
	Message string
}

func (e *UseCaseError) Error() string {
	return e.Message
}
