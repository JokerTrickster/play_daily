package _interface

import "github.com/labstack/echo/v4"

// ILikeRoomHandler 방 좋아요 추가 Handler 인터페이스
type ILikeRoomHandler interface {
	Like(c echo.Context) error
}

// IUnlikeRoomHandler 방 좋아요 삭제 Handler 인터페이스
type IUnlikeRoomHandler interface {
	Unlike(c echo.Context) error
}

// IGetLikedRoomsHandler 좋아요한 방 목록 조회 Handler 인터페이스
type IGetLikedRoomsHandler interface {
	GetLikedRooms(c echo.Context) error
}

// IGetRoomLikeStatusHandler 방 좋아요 상태 조회 Handler 인터페이스
type IGetRoomLikeStatusHandler interface {
	GetStatus(c echo.Context) error
}
