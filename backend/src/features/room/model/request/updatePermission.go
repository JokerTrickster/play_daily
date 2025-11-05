package request

type UpdatePermissionRequest struct {
	RoomID     int64  `json:"room_id" validate:"required"`
	UserID     int64  `json:"user_id" validate:"required"`
	Permission string `json:"permission" validate:"required,oneof=READ_ONLY READ_WRITE OWNER"`
}
