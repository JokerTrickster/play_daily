package response

// ResRoomMember 방 멤버 정보
type ResRoomMember struct {
	UserID          uint   `json:"user_id"`
	UserName        string `json:"user_name"`
	Email           string `json:"email"`
	ProfileImageURL string `json:"profile_image_url"`
	Permission      string `json:"permission"` // READ_ONLY, READ_WRITE, OWNER
	JoinedAt        int64  `json:"joined_at"`
}

// ResRoomMembers 방 멤버 목록 응답
type ResRoomMembers struct {
	RoomID  uint            `json:"room_id"`
	Members []ResRoomMember `json:"members"`
}
