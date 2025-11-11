package response

import "time"

// ResRoomListItem represents a room in the discovery list
type ResRoomListItem struct {
	ID         uint      `json:"id"`
	Name       string    `json:"name"`
	RoomCode   string    `json:"room_code"`
	IsPublic   bool      `json:"is_public"`
	LikesCount uint      `json:"likes_count"`
	OwnerID    uint      `json:"owner_id"`
	CreatedAt  time.Time `json:"created_at"`
}

// ResRoomList represents paginated room list response
type ResRoomList struct {
	Rooms   []ResRoomListItem `json:"rooms"`
	Total   int64             `json:"total"`
	Page    int               `json:"page"`
	Limit   int               `json:"limit"`
	HasNext bool              `json:"has_next"`
}
