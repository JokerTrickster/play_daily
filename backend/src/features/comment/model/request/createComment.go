package request

type ReqCreateComment struct {
	Content string `json:"content" validate:"required,min=1,max=1000"`
	Rating  uint8  `json:"rating" validate:"min=0,max=5"`
}
