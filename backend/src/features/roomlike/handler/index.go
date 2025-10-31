package handler

import (
	"main/common/db/mysql"
	"main/features/roomlike/repository"
	"main/features/roomlike/usecase"

	"github.com/labstack/echo/v4"
)

func NewRoomLikeHandler(c *echo.Echo) {
	db := mysql.GormMysqlDB

	// Repository 초기화
	roomLikeRepo := repository.NewRoomLikeRepository(db)

	// UseCase 초기화
	likeRoomUseCase := usecase.NewLikeRoomUseCase(roomLikeRepo)
	unlikeRoomUseCase := usecase.NewUnlikeRoomUseCase(roomLikeRepo)
	getLikedRoomsUseCase := usecase.NewGetLikedRoomsUseCase(roomLikeRepo)
	getRoomLikeStatusUseCase := usecase.NewGetRoomLikeStatusUseCase(roomLikeRepo)

	// Handler 초기화 (라우트 자동 등록)
	NewLikeRoomHandler(c, likeRoomUseCase)
	NewUnlikeRoomHandler(c, unlikeRoomUseCase)
	NewGetLikedRoomsHandler(c, getLikedRoomsUseCase)
	NewGetRoomLikeStatusHandler(c, getRoomLikeStatusUseCase)
}
