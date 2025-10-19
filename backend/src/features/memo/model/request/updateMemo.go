package request

type ReqUpdateMemo struct {
	Title        string   `json:"title"`
	Content      string   `json:"content"`
	ImageURL     string   `json:"image_url"`
	Rating       uint8    `json:"rating"`
	IsPinned     bool     `json:"is_pinned"`
	Latitude     *float64 `json:"latitude"`
	Longitude    *float64 `json:"longitude"`
	LocationName *string  `json:"location_name"`
}
