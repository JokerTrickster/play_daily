package mysql

import (
	"gorm.io/gorm"
)

// User 사용자 정보 테이블
type User struct {
	gorm.Model
	AccountID string  `json:"account_id" gorm:"column:account_id;type:varchar(255);uniqueIndex;not null;comment:계정 아이디"`
	Password  string  `json:"password" gorm:"column:password;type:varchar(255);not null;comment:암호화된 비밀번호"`
	Nickname  string  `json:"nickname" gorm:"column:nickname;type:varchar(100);comment:사용자 닉네임"`
	Memos     []Memo  `json:"memos,omitempty" gorm:"foreignKey:UserID;constraint:OnDelete:CASCADE"`
}

// TableName User 테이블명 지정
func (User) TableName() string {
	return "users"
}

// Memo 메모 정보 테이블
type Memo struct {
	gorm.Model
	UserID       uint     `json:"user_id" gorm:"column:user_id;not null;index;comment:작성자 ID"`
	Title        string   `json:"title" gorm:"column:title;type:varchar(200);not null;comment:메모 제목"`
	Content      string   `json:"content" gorm:"column:content;type:text;comment:메모 내용"`
	ImageURL     string   `json:"image_url" gorm:"column:image_url;type:varchar(500);comment:메모 이미지 URL"`
	Rating       uint8    `json:"rating" gorm:"column:rating;type:tinyint unsigned;default:0;index;comment:평점 (0-5)"`
	IsPinned     bool     `json:"is_pinned" gorm:"column:is_pinned;default:false;index;comment:고정 여부"`
	Latitude     *float64 `json:"latitude" gorm:"column:latitude;type:double;comment:위도"`
	Longitude    *float64 `json:"longitude" gorm:"column:longitude;type:double;comment:경도"`
	LocationName *string  `json:"location_name" gorm:"column:location_name;type:varchar(255);comment:위치 이름"`
	User         *User    `json:"user,omitempty" gorm:"foreignKey:UserID"`
}

// TableName Memo 테이블명 지정
func (Memo) TableName() string {
	return "memos"
}
