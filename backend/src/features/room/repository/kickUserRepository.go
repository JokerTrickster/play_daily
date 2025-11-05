package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"

	"gorm.io/gorm"
)

type KickUserRepository struct {
	GormDB *gorm.DB
}

func NewKickUserRepository(gormDB *gorm.DB) _interface.IKickUserRepository {
	return &KickUserRepository{
		GormDB: gormDB,
	}
}

// CheckRoomOwner 방 소유자인지 확인
func (r *KickUserRepository) CheckRoomOwner(ctx context.Context, roomID uint, userID uint) (bool, error) {
	var room mysql.Room
	result := r.GormDB.WithContext(ctx).
		Where("id = ? AND owner_user_id = ? AND deleted_at IS NULL", roomID, userID).
		First(&room)

	if result.Error != nil {
		if result.Error == gorm.ErrRecordNotFound {
			return false, nil
		}
		return false, result.Error
	}

	return true, nil
}

// KickUserFromRoom 사용자를 방에서 추방 (room_members에서 soft delete)
func (r *KickUserRepository) KickUserFromRoom(ctx context.Context, roomID uint, targetUserID uint) error {
	// room_members 테이블에서 해당 사용자의 레코드를 soft delete
	result := r.GormDB.WithContext(ctx).
		Model(&mysql.RoomMember{}).
		Where("room_id = ? AND user_id = ? AND deleted_at IS NULL", roomID, targetUserID).
		Update("deleted_at", gorm.Expr("CURRENT_TIMESTAMP"))

	if result.Error != nil {
		return result.Error
	}

	// 영향받은 행이 없으면 해당 사용자가 방에 없는 것
	if result.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}

	return nil
}
