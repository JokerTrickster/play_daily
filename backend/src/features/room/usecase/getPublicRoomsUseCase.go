package usecase

import (
	"context"
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"main/features/room/model/response"
	"time"
)

type GetPublicRoomsUseCase struct {
	GetPublicRoomsRepository _interface.IGetPublicRoomsRepository
	CtxTimeout               time.Duration
}

func NewGetPublicRoomsUseCase(repo _interface.IGetPublicRoomsRepository, timeout time.Duration) _interface.IGetPublicRoomsUseCase {
	return &GetPublicRoomsUseCase{
		GetPublicRoomsRepository: repo,
		CtxTimeout:               timeout,
	}
}

// GetPublicRooms retrieves paginated list of public rooms sorted by popularity
func (u *GetPublicRoomsUseCase) GetPublicRooms(ctx context.Context, req *request.ReqGetPublicRooms) (*response.ResGetPublicRooms, error) {
	ctx, cancel := context.WithTimeout(ctx, u.CtxTimeout)
	defer cancel()

	// Calculate offset
	offset := (req.Page - 1) * req.Limit

	// Fetch rooms with total count
	rooms, totalCount, err := u.GetPublicRoomsRepository.GetPublicRooms(ctx, offset, req.Limit)
	if err != nil {
		return nil, err
	}

	// Build response with room items
	roomItems := make([]response.PublicRoomItem, 0, len(rooms))
	for _, room := range rooms {
		// Get member count for each room
		memberCount, err := u.GetPublicRoomsRepository.GetRoomMemberCount(ctx, room.ID)
		if err != nil {
			memberCount = 0
		}

		// Build owner info
		ownerNickname := ""
		ownerProfileImage := ""
		if room.Owner != nil {
			ownerNickname = room.Owner.Nickname
			if room.Owner.ProfileImageURL != nil {
				ownerProfileImage = *room.Owner.ProfileImageURL
			}
		}

		roomItems = append(roomItems, response.PublicRoomItem{
			ID:          room.ID,
			RoomCode:    room.RoomCode,
			Name:        room.Name,
			LikesCount:  room.LikesCount,
			MemberCount: uint(memberCount),
			Owner: response.PublicRoomOwner{
				ID:           room.OwnerUserID,
				Nickname:     ownerNickname,
				ProfileImage: ownerProfileImage,
			},
		})
	}

	// Calculate pagination metadata
	totalPages := int(totalCount) / req.Limit
	if int(totalCount)%req.Limit > 0 {
		totalPages++
	}

	return &response.ResGetPublicRooms{
		Rooms: roomItems,
		Pagination: response.PaginationMeta{
			CurrentPage: req.Page,
			TotalPages:  totalPages,
			TotalCount:  int(totalCount),
			PerPage:     req.Limit,
		},
	}, nil
}
