package _interface

import (
	"context"
	"main/features/roomlike/model/response"
)

// ILikeRoomUseCase 방 좋아요 추가 UseCase 인터페이스
type ILikeRoomUseCase interface {
	Execute(ctx context.Context, roomID, userID uint) error
}

// IUnlikeRoomUseCase 방 좋아요 삭제 UseCase 인터페이스
type IUnlikeRoomUseCase interface {
	Execute(ctx context.Context, roomID, userID uint) error
}

// IGetLikedRoomsUseCase 좋아요한 방 목록 조회 UseCase 인터페이스
type IGetLikedRoomsUseCase interface {
	Execute(ctx context.Context, userID uint) ([]*response.ResLikedRoom, error)
}

// IGetRoomLikeStatusUseCase 방 좋아요 상태 조회 UseCase 인터페이스
type IGetRoomLikeStatusUseCase interface {
	Execute(ctx context.Context, roomID, userID uint) (*response.ResRoomLikeStatus, error)
}
