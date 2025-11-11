package _interface

import (
	"context"
	"main/features/room/model/request"
	"main/features/room/model/response"

	"github.com/labstack/echo/v4"
)

type IJoinRoomHandler interface {
	JoinRoom(c echo.Context) error
}

type IJoinRoomUseCase interface {
	JoinRoom(ctx context.Context, userID uint, req *request.ReqJoinRoom) (*response.ResRoom, error)
}

type IJoinRoomRepository interface {
	GetRoomByID(ctx context.Context, roomID uint) (*response.ResRoom, error)
	VerifyRoomPassword(ctx context.Context, roomID uint, password string) (bool, error)
	UpdateUserDefaultRoom(ctx context.Context, userID uint, roomID uint) error
	CheckRoomMemberExists(ctx context.Context, roomID uint, userID uint) (bool, error)
	AddRoomMember(ctx context.Context, roomID uint, userID uint) error
}

type IKickUserHandler interface {
	KickUser(c echo.Context) error
}

type IKickUserUseCase interface {
	KickUser(ctx context.Context, ownerUserID uint, req *request.ReqKickUser) error
}

type IKickUserRepository interface {
	CheckRoomOwner(ctx context.Context, roomID uint, userID uint) (bool, error)
	KickUserFromRoom(ctx context.Context, roomID uint, targetUserID uint) error
}

type IGetRoomMembersHandler interface {
	GetRoomMembers(c echo.Context) error
}

type IGetRoomMembersUseCase interface {
	GetRoomMembers(ctx context.Context, userID uint, roomID uint) (*response.ResRoomMembers, error)
}

type IGetRoomMembersRepository interface {
	GetRoomMembers(ctx context.Context, roomID uint) (*response.ResRoomMembers, error)
	CheckRoomMember(ctx context.Context, roomID uint, userID uint) (bool, error)
}

type IResetPasswordHandler interface {
	ResetPassword(c echo.Context) error
}

type IResetPasswordUseCase interface {
	ResetPassword(ctx context.Context, userID uint, req *request.ReqResetPassword, ipAddress *string, userAgent *string) (*response.ResResetPassword, error)
}

type IResetPasswordRepository interface {
	GenerateSecurePassword(ctx context.Context) (string, error)
	CheckRoomOwner(ctx context.Context, roomID uint, userID uint) (bool, error)
	GetRoomPassword(ctx context.Context, roomID uint) (string, error)
	CheckRecentResets(ctx context.Context, roomID uint, limit int, windowHours int) (int, error)
	ResetPassword(ctx context.Context, roomID uint, userID uint, newPassword string, previousPassword string, ipAddress *string, userAgent *string) error
	LogRateLimitViolation(ctx context.Context, roomID uint, userID uint, ipAddress *string, userAgent *string) error
}
