package handler

import (
	"main/common/db/mysql"
	_middleware "main/middleware"
	"main/features/room/repository"
	"main/features/room/usecase"
	"time"

	"github.com/labstack/echo/v4"
)

func NewRoomHandler(c *echo.Echo) {
	db := mysql.GormMysqlDB

	// Repository 초기화
	joinRoomRepo := repository.NewJoinRoomRepository(db)
	kickUserRepo := repository.NewKickUserRepository(db)
	getRoomMembersRepo := repository.NewGetRoomMembersRepository(db)
	updatePermissionRepo := repository.NewUpdatePermissionRepository(db)

	// UseCase 초기화
	joinRoomUseCase := usecase.NewJoinRoomUseCase(joinRoomRepo, 30*time.Second)
	kickUserUseCase := usecase.NewKickUserUseCase(kickUserRepo, 30*time.Second)
	getRoomMembersUseCase := usecase.NewGetRoomMembersUseCase(getRoomMembersRepo, 30*time.Second)
	updatePermissionUseCase := usecase.NewUpdatePermissionUseCase(updatePermissionRepo, 30*time.Second)

	// Handler 초기화
	joinRoomHandler := NewJoinRoomHandler(joinRoomUseCase)
	kickUserHandler := NewKickUserHandler(kickUserUseCase)
	getRoomMembersHandler := NewGetRoomMembersHandler(getRoomMembersUseCase)
	updatePermissionHandler := NewUpdatePermissionHandler(updatePermissionUseCase)

	// 라우트 등록 (인증 필요)
	c.POST("/v0.1/room/join", joinRoomHandler.JoinRoom, _middleware.TokenChecker)
	c.POST("/v0.1/room/kick", kickUserHandler.KickUser, _middleware.TokenChecker)
	c.GET("/v0.1/room/members", getRoomMembersHandler.GetRoomMembers, _middleware.TokenChecker)
	c.POST("/v0.1/room/permission", updatePermissionHandler.UpdatePermission, _middleware.TokenChecker)
}
