package request

// ReqKickUser 사용자 추방 요청
type ReqKickUser struct {
	RoomID       uint `json:"room_id" validate:"required"`       // 방 ID
	TargetUserID uint `json:"target_user_id" validate:"required"` // 추방할 사용자 ID
}
