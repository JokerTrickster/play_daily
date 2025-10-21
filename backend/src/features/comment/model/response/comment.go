package response

import "time"

type ResComment struct {
	ID        uint      `json:"id"`
	MemoID    uint      `json:"memo_id"`
	UserID    uint      `json:"user_id"`
	UserName  string    `json:"user_name"`
	Content   string    `json:"content"`
	Rating    uint8     `json:"rating"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type ResCommentList struct {
	Comments []ResComment `json:"comments"`
	Total    int64        `json:"total"`
}
