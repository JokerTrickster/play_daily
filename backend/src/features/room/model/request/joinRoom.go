package request

type ReqJoinRoom struct {
	RoomID       uint   `json:"room_id" validate:"required"`
	RoomPassword string `json:"room_password" validate:"required,len=4"`
}
