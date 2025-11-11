package _interface

import (
	"context"
	"main/features/room/model/response"
)

type IRoomDiscoveryUseCase interface {
	GetPopularRooms(ctx context.Context, page int, limit int) (*response.ResRoomList, error)
	GetRecentRooms(ctx context.Context, page int, limit int) (*response.ResRoomList, error)
}
