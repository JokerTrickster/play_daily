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
}
