package repository

import (
	"context"
	"errors"
	"main/common/db/mysql"
	_interface "main/features/roomlike/model/interface"
	"main/features/roomlike/model/response"

	"gorm.io/gorm"
)

type RoomLikeRepository struct {
	GormDB *gorm.DB
}

func NewRoomLikeRepository(gormDB *gorm.DB) _interface.IRoomLikeRepository {
	return &RoomLikeRepository{
		GormDB: gormDB,
	}
}

// CreateRoomLike 방 좋아요 추가
func (r *RoomLikeRepository) CreateRoomLike(ctx context.Context, roomID, userID uint) error {
	roomLike := &mysql.RoomLike{
		RoomID: roomID,
		UserID: userID,
	}

	// 중복 체크 (UNIQUE 제약 조건이 있지만 명시적으로 체크)
	var count int64
	err := r.GormDB.WithContext(ctx).
		Model(&mysql.RoomLike{}).
		Where("room_id = ? AND user_id = ?", roomID, userID).
		Count(&count).Error

	if err != nil {
		return err
	}

	if count > 0 {
		return errors.New("already liked this room")
	}

	// 좋아요 추가
	err = r.GormDB.WithContext(ctx).Create(roomLike).Error
	if err != nil {
		return err
	}

	// Room의 likes_count 증가
	err = r.GormDB.WithContext(ctx).
		Model(&mysql.Room{}).
		Where("id = ?", roomID).
		UpdateColumn("likes_count", gorm.Expr("likes_count + ?", 1)).
		Error

	return err
}

// DeleteRoomLike 방 좋아요 삭제
func (r *RoomLikeRepository) DeleteRoomLike(ctx context.Context, roomID, userID uint) error {
	result := r.GormDB.WithContext(ctx).
		Where("room_id = ? AND user_id = ?", roomID, userID).
		Delete(&mysql.RoomLike{})

	if result.Error != nil {
		return result.Error
	}

	if result.RowsAffected == 0 {
		return errors.New("room like not found")
	}

	// Room의 likes_count 감소
	err := r.GormDB.WithContext(ctx).
		Model(&mysql.Room{}).
		Where("id = ? AND likes_count > 0", roomID).
		UpdateColumn("likes_count", gorm.Expr("likes_count - ?", 1)).
		Error

	return err
}

// GetLikedRooms 사용자가 좋아요한 방 목록 조회
func (r *RoomLikeRepository) GetLikedRooms(ctx context.Context, userID uint) ([]*response.ResLikedRoom, error) {
	var results []*response.ResLikedRoom

	err := r.GormDB.WithContext(ctx).
		Table("room_likes").
		Select(`
			rooms.id as room_id,
			rooms.room_code,
			rooms.name as room_name,
			rooms.owner_user_id as owner_id,
			COALESCE(rooms.likes_count, 0) as likes_count,
			FROM_UNIXTIME(room_likes.created_at) as liked_at
		`).
		Joins("JOIN rooms ON rooms.id = room_likes.room_id").
		Where("room_likes.user_id = ? AND rooms.deleted_at IS NULL", userID).
		Order("room_likes.created_at DESC").
		Scan(&results).Error

	return results, err
}

// CheckRoomLikeStatus 방 좋아요 상태 확인
func (r *RoomLikeRepository) CheckRoomLikeStatus(ctx context.Context, roomID, userID uint) (*response.ResRoomLikeStatus, error) {
	// 좋아요 여부 확인
	var count int64
	err := r.GormDB.WithContext(ctx).
		Model(&mysql.RoomLike{}).
		Where("room_id = ? AND user_id = ?", roomID, userID).
		Count(&count).Error

	if err != nil {
		return nil, err
	}

	// 총 좋아요 개수 조회
	likesCount, err := r.GetRoomLikesCount(ctx, roomID)
	if err != nil {
		return nil, err
	}

	return &response.ResRoomLikeStatus{
		IsLiked:    count > 0,
		LikesCount: likesCount,
	}, nil
}

// GetRoomLikesCount 방의 총 좋아요 개수 조회
func (r *RoomLikeRepository) GetRoomLikesCount(ctx context.Context, roomID uint) (int, error) {
	var room mysql.Room
	err := r.GormDB.WithContext(ctx).
		Select("likes_count").
		Where("id = ?", roomID).
		First(&room).Error

	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return 0, errors.New("room not found")
		}
		return 0, err
	}

	return int(room.LikesCount), nil
}
