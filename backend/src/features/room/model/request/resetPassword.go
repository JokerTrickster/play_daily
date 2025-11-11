package request

type ReqResetPassword struct {
	RoomID uint `json:"room_id" validate:"required"`
}
