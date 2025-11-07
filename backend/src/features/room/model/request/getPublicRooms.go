package request

type ReqGetPublicRooms struct {
	Page  int `query:"page"`
	Limit int `query:"limit"`
}
