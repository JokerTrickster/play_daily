package usecase

import (
	"context"
	"errors"
	"fmt"
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"main/features/room/model/response"
	"time"
)

type ResetPasswordUseCase struct {
	Repository     _interface.IResetPasswordRepository
	ContextTimeout time.Duration
}

func NewResetPasswordUseCase(repo _interface.IResetPasswordRepository, timeout time.Duration) _interface.IResetPasswordUseCase {
	return &ResetPasswordUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

func (uc *ResetPasswordUseCase) ResetPassword(ctx context.Context, userID uint, req *request.ReqResetPassword, ipAddress *string, userAgent *string) (*response.ResResetPassword, error) {
	_, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// 1. Verify room ownership
	isOwner, err := uc.Repository.CheckRoomOwner(ctx, req.RoomID, userID)
	if err != nil {
		return nil, err
	}
	if !isOwner {
		return nil, errors.New("only room owner can reset password")
	}

	// 2. Check rate limiting (3 resets per 24 hours)
	const (
		resetLimit      = 3
		windowHours     = 24
	)
	recentResets, err := uc.Repository.CheckRecentResets(ctx, req.RoomID, resetLimit, windowHours)
	if err != nil {
		return nil, err
	}

	if recentResets >= resetLimit {
		// Log rate limit violation
		_ = uc.Repository.LogRateLimitViolation(ctx, req.RoomID, userID, ipAddress, userAgent)
		return nil, fmt.Errorf("rate limit exceeded: maximum %d resets per %d hours", resetLimit, windowHours)
	}

	// 3. Get current password for audit log
	previousPassword, err := uc.Repository.GetRoomPassword(ctx, req.RoomID)
	if err != nil {
		return nil, err
	}

	// 4. Generate new secure password
	newPassword, err := uc.Repository.GenerateSecurePassword(ctx)
	if err != nil {
		return nil, errors.New("failed to generate secure password")
	}

	// 5. Update password and create audit log (in transaction)
	err = uc.Repository.ResetPassword(ctx, req.RoomID, userID, newPassword, previousPassword, ipAddress, userAgent)
	if err != nil {
		return nil, err
	}

	// 6. Return success response with new password (only once!)
	return &response.ResResetPassword{
		Success:     true,
		NewPassword: newPassword,
		Message:     "Password reset successfully",
	}, nil
}
