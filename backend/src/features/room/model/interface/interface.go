package _interface

import (
	"context"
	"main/common/db/mysql"
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
	GetRoomByRoomCode(ctx context.Context, roomCode string) (*mysql.Room, error)
	FindExistingMember(ctx context.Context, roomID uint, userID uint) (*mysql.RoomMember, error)
	CreateRoomMember(ctx context.Context, member *mysql.RoomMember) error
	GetRoomMemberCount(ctx context.Context, roomID uint) (int64, error)
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

type IUpdatePermissionHandler interface {
	UpdatePermission(c echo.Context) error
}

type IUpdatePermissionUseCase interface {
	Execute(ctx context.Context, requesterUserID int64, req *request.UpdatePermissionRequest) error
}

// New interfaces for privacy management
type IUpdateRoomPrivacyHandler interface {
	UpdateRoomPrivacy(c echo.Context) error
}

type IUpdateRoomPrivacyUseCase interface {
	UpdateRoomPrivacy(ctx context.Context, roomID uint, userID uint, req *request.ReqUpdateRoomPrivacy) (*response.ResUpdateRoomPrivacy, error)
}

type IUpdateRoomPrivacyRepository interface {
	GetRoomByID(ctx context.Context, roomID uint) (*mysql.Room, error)
	UpdateRoom(ctx context.Context, room *mysql.Room) error
}

type IGetPublicRoomsHandler interface {
	GetPublicRooms(c echo.Context) error
}

type IGetPublicRoomsUseCase interface {
	GetPublicRooms(ctx context.Context, req *request.ReqGetPublicRooms) (*response.ResGetPublicRooms, error)
}

type IGetPublicRoomsRepository interface {
	GetPublicRooms(ctx context.Context, offset, limit int) ([]mysql.Room, int64, error)
	GetRoomMemberCount(ctx context.Context, roomID uint) (int64, error)
}
