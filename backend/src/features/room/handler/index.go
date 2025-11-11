package handler

import (
	"main/common/db/mysql"
	"main/features/room/repository"
	"main/features/room/usecase"
	_middleware "main/middleware"
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
	updateRoomRepo := repository.NewUpdateRoomRepository(db)
	roomDiscoveryRepo := repository.NewRoomDiscoveryRepository(db)
	resetPasswordRepo := repository.NewResetPasswordRepository(db)

	// UseCase 초기화
	joinRoomUseCase := usecase.NewJoinRoomUseCase(joinRoomRepo, 30*time.Second)
	kickUserUseCase := usecase.NewKickUserUseCase(kickUserRepo, 30*time.Second)
	getRoomMembersUseCase := usecase.NewGetRoomMembersUseCase(getRoomMembersRepo, 30*time.Second)
	updatePermissionUseCase := usecase.NewUpdatePermissionUseCase(updatePermissionRepo, 30*time.Second)
	updateRoomUseCase := usecase.NewUpdateRoomUseCase(updateRoomRepo, 30*time.Second)
	roomDiscoveryUseCase := usecase.NewRoomDiscoveryUseCase(roomDiscoveryRepo, 30*time.Second)
	resetPasswordUseCase := usecase.NewResetPasswordUseCase(resetPasswordRepo, 30*time.Second)

	// Handler 초기화
	joinRoomHandler := NewJoinRoomHandler(joinRoomUseCase)
	kickUserHandler := NewKickUserHandler(kickUserUseCase)
	getRoomMembersHandler := NewGetRoomMembersHandler(getRoomMembersUseCase)
	updatePermissionHandler := NewUpdatePermissionHandler(updatePermissionUseCase)
	updateRoomHandler := NewUpdateRoomHandler(updateRoomUseCase)
	resetPasswordHandler := NewResetPasswordHandler(resetPasswordUseCase)

	// Discovery Handler (No authentication required)
	NewRoomDiscoveryHandler(c, roomDiscoveryUseCase)

	// 라우트 등록 (인증 필요)
	c.POST("/v0.1/room/join", joinRoomHandler.JoinRoom, _middleware.TokenChecker)
	c.POST("/v0.1/room/kick", kickUserHandler.KickUser, _middleware.TokenChecker)
	c.GET("/v0.1/room/members", getRoomMembersHandler.GetRoomMembers, _middleware.TokenChecker)
	c.POST("/v0.1/room/permission", updatePermissionHandler.UpdatePermission, _middleware.TokenChecker)
	c.POST("/v0.1/room/update", updateRoomHandler.UpdateRoom, _middleware.TokenChecker)
	c.POST("/v0.1/rooms/:id/reset-password", resetPasswordHandler.ResetPassword, _middleware.TokenChecker)
}
