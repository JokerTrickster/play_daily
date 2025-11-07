package response

type PublicRoomOwner struct {
	ID           uint   `json:"id"`
	Nickname     string `json:"nickname"`
	ProfileImage string `json:"profile_image"`
}

type PublicRoomItem struct {
	ID          uint            `json:"id"`
	RoomCode    string          `json:"room_code"`
	Name        string          `json:"name"`
	LikesCount  uint            `json:"likes_count"`
	MemberCount uint            `json:"member_count"`
	Owner       PublicRoomOwner `json:"owner"`
}

type PaginationMeta struct {
	CurrentPage int `json:"current_page"`
	TotalPages  int `json:"total_pages"`
	TotalCount  int `json:"total_count"`
	PerPage     int `json:"per_page"`
}

type ResGetPublicRooms struct {
	Rooms      []PublicRoomItem `json:"rooms"`
	Pagination PaginationMeta   `json:"pagination"`
}
