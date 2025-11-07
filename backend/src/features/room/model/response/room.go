package response

type ResRoom struct {
	ID           uint   `json:"id"`
	RoomCode     string `json:"room_code"`
	Name         string `json:"name"`
	IsPublic     bool   `json:"is_public"`
	LikesCount   uint   `json:"likes_count"`
	MemberCount  uint   `json:"member_count"`
	Permission   string `json:"permission"`
	IsNewMember  bool   `json:"is_new_member"`
}
