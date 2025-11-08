package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/memo/model/interface"

	"gorm.io/gorm"
)

type CreateMemoRepository struct {
	GormDB *gorm.DB
}

func NewCreateMemoRepository(gormDB *gorm.DB) _interface.ICreateMemoRepository {
	return &CreateMemoRepository{
		GormDB: gormDB,
	}
}

// Create 메모 생성
func (r *CreateMemoRepository) Create(ctx context.Context, memo *mysql.Memo) error {
	result := r.GormDB.WithContext(ctx).Create(memo)
	return result.Error
}

// CreateWithCategories 메모와 카테고리 연결을 트랜잭션으로 생성
func (r *CreateMemoRepository) CreateWithCategories(ctx context.Context, memo *mysql.Memo, categoryIDs []uint) error {
	return r.GormDB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 1. 메모 생성
		if err := tx.Create(memo).Error; err != nil {
			return err
		}

		// 2. 카테고리 연결 생성
		for _, categoryID := range categoryIDs {
			selection := &mysql.MemoCategorySelection{
				MemoID:     memo.ID,
				CategoryID: categoryID,
			}
			if err := tx.Create(selection).Error; err != nil {
				return err
			}
		}

		return nil
	})
}

// CheckWritePermission 사용자가 방에 쓰기 권한이 있는지 확인
// 1. 방 소유자(OWNER)이거나
// 2. READ_WRITE 권한을 가진 멤버인 경우 true 반환
func (r *CreateMemoRepository) CheckWritePermission(ctx context.Context, roomID uint, userID uint) (bool, error) {
	// 1. 방 소유자인지 확인
	var room mysql.Room
	if err := r.GormDB.WithContext(ctx).Where("id = ? AND owner_user_id = ?", roomID, userID).First(&room).Error; err == nil {
		return true, nil // 소유자는 항상 쓰기 권한 있음
	}

	// 2. room_members에서 권한 확인
	var member mysql.RoomMember
	err := r.GormDB.WithContext(ctx).
		Where("room_id = ? AND user_id = ? AND deleted_at IS NULL", roomID, userID).
		First(&member).Error

	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return false, nil // 멤버가 아님
		}
		return false, err
	}

	// READ_WRITE 또는 OWNER 권한이면 쓰기 가능
	return member.Permission == mysql.PermissionReadWrite || member.Permission == mysql.PermissionOwner, nil
}
