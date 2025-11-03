package response

type ResRoom struct {
	ID           uint   `json:"id"`
	OwnerID      uint   `json:"owner_id"`
	RoomPassword string `json:"room_password"`
}
