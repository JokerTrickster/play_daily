package _interface

import (
	"context"
	"main/common/db/mysql"
)

type IRoomDiscoveryRepository interface {
	GetPopularRooms(ctx context.Context, offset int, limit int) ([]mysql.Room, error)
	GetRecentRooms(ctx context.Context, offset int, limit int) ([]mysql.Room, error)
	CountPublicRooms(ctx context.Context) (int64, error)
}
