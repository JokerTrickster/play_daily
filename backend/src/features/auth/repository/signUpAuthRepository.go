package repository

import (
	"context"
	"errors"
	"fmt"
	"main/common/db/mysql"
	_interface "main/features/auth/model/interface"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type SignUpAuthRepository struct {
	GormDB *gorm.DB
}

func NewSignUpAuthRepository(gormDB *gorm.DB) _interface.ISignUpAuthRepository {
	return &SignUpAuthRepository{
		GormDB: gormDB,
	}
}

func (r *SignUpAuthRepository) CheckAccountIDDuplicate(ctx context.Context, accountID string) error {
	var user mysql.User

	// account_id로 사용자 조회
	result := r.GormDB.WithContext(ctx).
		Where("account_id = ?", accountID).
		First(&user)

	// 사용자가 있으면 중복 에러 반환
	if result.Error == nil {
		return errors.New("account ID already exists")
	}
	
	// 사용자가 없으면 nil 반환
	return nil
}

func (r *SignUpAuthRepository) CreateUser(ctx context.Context, userDTO *mysql.User) (*mysql.User, error) {
	// 트랜잭션 시작
	err := r.GormDB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 1. 사용자 생성
		if err := tx.Create(userDTO).Error; err != nil {
			return err
		}

		// 2. 사용자의 기본 방 생성
		roomCode := uuid.New().String()
		roomName := fmt.Sprintf("%s의 방", userDTO.Nickname)

		room := &mysql.Room{
			RoomCode:    roomCode,
			Name:        roomName,
			OwnerUserID: userDTO.ID,
		}

		if err := tx.Create(room).Error; err != nil {
			return err
		}

		// 3. 사용자의 default_room_id 업데이트
		userDTO.DefaultRoomID = &room.ID
		if err := tx.Save(userDTO).Error; err != nil {
			return err
		}

		return nil
	})

	if err != nil {
		return nil, err
	}

	// 생성된 사용자 정보 반환 (ID와 DefaultRoomID가 자동 할당됨)
	return userDTO, nil
}
