package mysql

import (
	"gorm.io/gorm"
)

// Room 방 정보 테이블
type Room struct {
	gorm.Model
	RoomCode    string `json:"room_code" gorm:"column:room_code;type:varchar(50);uniqueIndex;not null;comment:방 고유 코드 (UUID)"`
	Name        string `json:"name" gorm:"column:name;type:varchar(100);not null;comment:방 이름"`
	OwnerUserID uint   `json:"owner_user_id" gorm:"column:owner_user_id;not null;index;comment:방 소유자 ID"`
	Owner       *User  `json:"owner,omitempty" gorm:"foreignKey:OwnerUserID"`
	Memos       []Memo `json:"memos,omitempty" gorm:"foreignKey:RoomID;constraint:OnDelete:CASCADE"`
}

// TableName Room 테이블명 지정
func (Room) TableName() string {
	return "rooms"
}

// User 사용자 정보 테이블
type User struct {
	gorm.Model
	AccountID        string  `json:"account_id" gorm:"column:account_id;type:varchar(255);uniqueIndex;not null;comment:계정 아이디"`
	Password         string  `json:"password" gorm:"column:password;type:varchar(255);not null;comment:암호화된 비밀번호"`
	Nickname         string  `json:"nickname" gorm:"column:nickname;type:varchar(100);comment:사용자 닉네임"`
	ProfileImageURL  *string `json:"profile_image_url,omitempty" gorm:"column:profile_image_url;type:varchar(500);comment:프로필 이미지 URL"`
	DefaultRoomID    *uint   `json:"default_room_id" gorm:"column:default_room_id;index;comment:기본 방 ID (회원가입 시 자동 생성된 방)"`
	DefaultRoom      *Room   `json:"default_room,omitempty" gorm:"foreignKey:DefaultRoomID"`
	Memos            []Memo  `json:"memos,omitempty" gorm:"foreignKey:UserID;constraint:OnDelete:CASCADE"`
}

// TableName User 테이블명 지정
func (User) TableName() string {
	return "users"
}

// Memo 메모 정보 테이블
type Memo struct {
	gorm.Model
	UserID          uint      `json:"user_id" gorm:"column:user_id;not null;index;comment:작성자 ID"`
	RoomID          uint      `json:"room_id" gorm:"column:room_id;not null;index;comment:메모가 속한 방 ID"`
	Title           string    `json:"title" gorm:"column:title;type:varchar(200);not null;comment:메모 제목"`
	Content         string    `json:"content" gorm:"column:content;type:text;comment:메모 내용"`
	ImageURL        string    `json:"image_url" gorm:"column:image_url;type:varchar(500);comment:메모 이미지 URL"`
	Rating          uint8     `json:"rating" gorm:"column:rating;type:tinyint unsigned;default:0;index;comment:평점 (0-5) 또는 관심도 (1-5)"`
	IsPinned        bool      `json:"is_pinned" gorm:"column:is_pinned;default:false;index;comment:고정 여부"`
	Latitude        *float64  `json:"latitude" gorm:"column:latitude;type:double;comment:위도"`
	Longitude       *float64  `json:"longitude" gorm:"column:longitude;type:double;comment:경도"`
	LocationName    *string   `json:"location_name" gorm:"column:location_name;type:varchar(255);comment:위치 이름"`
	Category        *string   `json:"category" gorm:"column:category;type:varchar(50);comment:장소 카테고리"`
	// Wishlist fields (Issue #19)
	IsWishlist      bool      `json:"is_wishlist" gorm:"column:is_wishlist;default:false;not null;index:idx_user_wishlist;comment:위시리스트 여부 (true=가고싶은곳, false=방문한곳)"`
	BusinessName    *string   `json:"business_name,omitempty" gorm:"column:business_name;type:varchar(255);comment:장소/가게명 (카카오/네이버)"`
	BusinessPhone   *string   `json:"business_phone,omitempty" gorm:"column:business_phone;type:varchar(50);comment:전화번호"`
	BusinessAddress *string   `json:"business_address,omitempty" gorm:"column:business_address;type:text;comment:주소"`
	NaverPlaceURL   *string   `json:"naver_place_url,omitempty" gorm:"column:naver_place_url;type:varchar(500);comment:네이버 플레이스 URL"`
	User            *User     `json:"user,omitempty" gorm:"foreignKey:UserID"`
	Room            *Room     `json:"room,omitempty" gorm:"foreignKey:RoomID"`
	Comments        []Comment `json:"comments,omitempty" gorm:"foreignKey:MemoID;constraint:OnDelete:CASCADE"`
}

// TableName Memo 테이블명 지정
func (Memo) TableName() string {
	return "memos"
}

// Comment 댓글 정보 테이블
type Comment struct {
	gorm.Model
	MemoID   uint   `json:"memo_id" gorm:"column:memo_id;not null;index;comment:메모 ID"`
	UserID   uint   `json:"user_id" gorm:"column:user_id;not null;index;comment:작성자 ID"`
	Content  string `json:"content" gorm:"column:content;type:text;not null;comment:댓글 내용"`
	Rating   uint8  `json:"rating" gorm:"column:rating;type:tinyint unsigned;default:0;comment:댓글 작성자의 평점 (0-5)"`
	Memo     *Memo  `json:"memo,omitempty" gorm:"foreignKey:MemoID"`
	User     *User  `json:"user,omitempty" gorm:"foreignKey:UserID"`
}

// TableName Comment 테이블명 지정
func (Comment) TableName() string {
	return "comments"
}
