package response

import "time"

// ResRoomLike 방 좋아요 응답
type ResRoomLike struct {
	ID        uint      `json:"id"`
	RoomID    uint      `json:"room_id"`
	UserID    uint      `json:"user_id"`
	CreatedAt time.Time `json:"created_at"`
}

// ResLikedRoom 좋아요한 방 정보 응답
type ResLikedRoom struct {
	RoomID     uint      `json:"room_id"`
	RoomCode   string    `json:"room_code"`
	RoomName   string    `json:"room_name"`
	OwnerID    uint      `json:"owner_id"`
	LikesCount int       `json:"likes_count"`
	LikedAt    time.Time `json:"liked_at"`
}

// ResRoomLikeStatus 방 좋아요 상태 응답
type ResRoomLikeStatus struct {
	IsLiked    bool `json:"is_liked"`
	LikesCount int  `json:"likes_count"`
}
