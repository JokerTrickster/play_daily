package request

type ReqJoinRoom struct {
	RoomCode string  `json:"room_code" validate:"required"`
	Password *string `json:"password,omitempty"`
}
