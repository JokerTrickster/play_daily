package response

type ResUpdateRoomPrivacy struct {
	ID       uint    `json:"id"`
	RoomCode string  `json:"room_code"`
	IsPublic bool    `json:"is_public"`
	Password *string `json:"password,omitempty"`
}
