package _interface

import (
	"context"
	"main/features/roomlike/model/response"
)

// IRoomLikeRepository 방 좋아요 Repository 인터페이스
type IRoomLikeRepository interface {
	// CreateRoomLike 방 좋아요 추가
	CreateRoomLike(ctx context.Context, roomID, userID uint) error

	// DeleteRoomLike 방 좋아요 삭제
	DeleteRoomLike(ctx context.Context, roomID, userID uint) error

	// GetLikedRooms 사용자가 좋아요한 방 목록 조회
	GetLikedRooms(ctx context.Context, userID uint) ([]*response.ResLikedRoom, error)

	// CheckRoomLikeStatus 방 좋아요 상태 확인
	CheckRoomLikeStatus(ctx context.Context, roomID, userID uint) (*response.ResRoomLikeStatus, error)

	// GetRoomLikesCount 방의 총 좋아요 개수 조회
	GetRoomLikesCount(ctx context.Context, roomID uint) (int, error)
}
