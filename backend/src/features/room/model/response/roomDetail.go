package response

import "time"

type ResRoomDetail struct {
	ID         uint      `json:"id"`
	Name       string    `json:"name"`
	RoomCode   string    `json:"room_code"`
	IsPublic   bool      `json:"is_public"`
	LikesCount uint      `json:"likes_count"`
	OwnerID    uint      `json:"owner_id"`
	OwnerName  string    `json:"owner_name"` // owner's account_id
	OwnerBio   *string   `json:"owner_bio"`  // owner's bio for room promotion
	CreatedAt  time.Time `json:"created_at"`
}
