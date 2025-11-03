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

	// UseCase 초기화
	joinRoomUseCase := usecase.NewJoinRoomUseCase(joinRoomRepo, 30*time.Second)

	// Handler 초기화
	joinRoomHandler := NewJoinRoomHandler(joinRoomUseCase)

	// 라우트 등록 (인증 필요)
	c.POST("/v0.1/room/join", joinRoomHandler.JoinRoom, _middleware.TokenChecker)
}
