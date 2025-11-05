package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"
	"main/features/room/model/response"

	"gorm.io/gorm"
)

type GetRoomMembersRepository struct {
	GormDB *gorm.DB
}

func NewGetRoomMembersRepository(gormDB *gorm.DB) _interface.IGetRoomMembersRepository {
	return &GetRoomMembersRepository{
		GormDB: gormDB,
	}
}

// GetRoomMembers 방 멤버 목록 조회
func (r *GetRoomMembersRepository) GetRoomMembers(ctx context.Context, roomID uint) (*response.ResRoomMembers, error) {
	var members []mysql.RoomMember

	// room_members와 users를 조인하여 멤버 정보 조회
	result := r.GormDB.WithContext(ctx).
		Preload("User").
		Where("room_id = ? AND deleted_at IS NULL", roomID).
		Order("permission DESC, joined_at ASC"). // OWNER > READ_WRITE > READ_ONLY, 참여 시간 순
		Find(&members)

	if result.Error != nil {
		return nil, result.Error
	}

	// 응답 구조체로 변환
	resMembers := make([]response.ResRoomMember, 0, len(members))
	for _, member := range members {
		if member.User != nil {
			resMembers = append(resMembers, response.ResRoomMember{
				UserID:     member.UserID,
				UserName:   member.User.Nickname,
				Email:      member.User.AccountID,
				Permission: string(member.Permission),
				JoinedAt:   member.JoinedAt,
			})
		}
	}

	return &response.ResRoomMembers{
		RoomID:  roomID,
		Members: resMembers,
	}, nil
}

// CheckRoomMember 사용자가 방 멤버인지 확인
func (r *GetRoomMembersRepository) CheckRoomMember(ctx context.Context, roomID uint, userID uint) (bool, error) {
	var count int64
	result := r.GormDB.WithContext(ctx).
		Model(&mysql.RoomMember{}).
		Where("room_id = ? AND user_id = ? AND deleted_at IS NULL", roomID, userID).
		Count(&count)

	if result.Error != nil {
		return false, result.Error
	}

	return count > 0, nil
}
