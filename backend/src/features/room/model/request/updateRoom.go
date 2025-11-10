package request

type UpdateRoomRequest struct {
	RoomID       int64  `json:"room_id" validate:"required"`
	IsPublic     *bool  `json:"is_public"`
	RoomPassword string `json:"room_password"`
}
