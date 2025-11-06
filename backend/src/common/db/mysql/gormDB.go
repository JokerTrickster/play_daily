package mysql

import (
	"time"

	"gorm.io/gorm"
)

// Room 방 정보 테이블
type Room struct {
	gorm.Model
	RoomCode    string `json:"room_code" gorm:"column:room_code;type:varchar(50);uniqueIndex;not null;comment:방 고유 코드 (UUID)"`
	Name        string `json:"name" gorm:"column:name;type:varchar(100);not null;comment:방 이름"`
	LikesCount  uint   `json:"likes_count" gorm:"column:likes_count;type:int unsigned;default:0;index;comment:좋아요 개수"`
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
	RoomPassword     string  `json:"room_password" gorm:"column:room_password;type:varchar(4);not null;comment:방 비밀번호 (4자리 숫자)"`
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
	UserID          uint            `json:"user_id" gorm:"column:user_id;not null;index;comment:작성자 ID"`
	RoomID          uint            `json:"room_id" gorm:"column:room_id;not null;index;comment:메모가 속한 방 ID"`
	Title           string          `json:"title" gorm:"column:title;type:varchar(200);not null;comment:메모 제목"`
	Content         string          `json:"content" gorm:"column:content;type:text;comment:메모 내용"` // Deprecated
	CreationMode    string          `json:"creation_mode" gorm:"column:creation_mode;type:enum('map','list');not null;default:'list';comment:생성 방식 (map/list)"` // NEW
	ImageURL        string          `json:"image_url" gorm:"column:image_url;type:varchar(500);comment:메모 이미지 URL"`
	Rating          uint8           `json:"rating" gorm:"column:rating;type:tinyint unsigned;default:0;index;comment:평점 (0-5) 또는 관심도 (1-5)"`
	IsPinned        bool            `json:"is_pinned" gorm:"column:is_pinned;default:false;index;comment:고정 여부"`
	LikesCount      uint            `json:"likes_count" gorm:"column:likes_count;type:int unsigned;default:0;index;comment:좋아요 개수"`
	Latitude        *float64        `json:"latitude" gorm:"column:latitude;type:double;comment:위도"`
	Longitude       *float64        `json:"longitude" gorm:"column:longitude;type:double;comment:경도"`
	LocationName    *string         `json:"location_name" gorm:"column:location_name;type:varchar(255);comment:위치 이름"`
	Category        *string         `json:"category" gorm:"column:category;type:varchar(50);comment:장소 카테고리"` // Deprecated
	// Wishlist fields (Issue #19)
	IsWishlist      bool            `json:"is_wishlist" gorm:"column:is_wishlist;default:false;not null;index:idx_user_wishlist;comment:위시리스트 여부 (true=가고싶은곳, false=방문한곳)"`
	BusinessName    *string         `json:"business_name,omitempty" gorm:"column:business_name;type:varchar(255);comment:장소/가게명 (카카오/네이버)"`
	BusinessPhone   *string         `json:"business_phone,omitempty" gorm:"column:business_phone;type:varchar(50);comment:전화번호"`
	BusinessAddress *string         `json:"business_address,omitempty" gorm:"column:business_address;type:text;comment:주소"`
	NaverPlaceURL   *string         `json:"naver_place_url,omitempty" gorm:"column:naver_place_url;type:varchar(500);comment:네이버 플레이스 URL"`
	User            *User           `json:"user,omitempty" gorm:"foreignKey:UserID"`
	Room            *Room           `json:"room,omitempty" gorm:"foreignKey:RoomID"`
	Comments        []Comment       `json:"comments,omitempty" gorm:"foreignKey:MemoID;constraint:OnDelete:CASCADE"`
	Categories      []MemoCategory  `json:"categories,omitempty" gorm:"many2many:memo_category_selections;"` // NEW: Many-to-many 관계
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

// RoomLike 방 좋아요 정보 테이블
type RoomLike struct {
	ID        uint  `json:"id" gorm:"column:id;primaryKey;autoIncrement"`
	RoomID    uint  `json:"room_id" gorm:"column:room_id;not null;index;comment:좋아요한 방 ID"`
	UserID    uint  `json:"user_id" gorm:"column:user_id;not null;index;comment:좋아요를 누른 사용자 ID"`
	CreatedAt int64 `json:"created_at" gorm:"column:created_at;autoCreateTime;comment:좋아요 누른 시간"`
	Room      *Room `json:"room,omitempty" gorm:"foreignKey:RoomID"`
	User      *User `json:"user,omitempty" gorm:"foreignKey:UserID"`
}

// TableName RoomLike 테이블명 지정
func (RoomLike) TableName() string {
	return "room_likes"
}

// MemoLike 메모 좋아요 정보 테이블
type MemoLike struct {
	ID        uint  `json:"id" gorm:"column:id;primaryKey;autoIncrement"`
	MemoID    uint  `json:"memo_id" gorm:"column:memo_id;not null;index;comment:좋아요한 메모 ID"`
	UserID    uint  `json:"user_id" gorm:"column:user_id;not null;index;comment:좋아요를 누른 사용자 ID"`
	CreatedAt int64 `json:"created_at" gorm:"column:created_at;autoCreateTime;comment:좋아요 누른 시간"`
	Memo      *Memo `json:"memo,omitempty" gorm:"foreignKey:MemoID"`
	User      *User `json:"user,omitempty" gorm:"foreignKey:UserID"`
}

// TableName MemoLike 테이블명 지정
func (MemoLike) TableName() string {
	return "memo_likes"
}

// RoomPermission 방 권한 레벨
type RoomPermission string

const (
	PermissionReadOnly  RoomPermission = "READ_ONLY"  // 읽기 전용 (공유 보기만 가능)
	PermissionReadWrite RoomPermission = "READ_WRITE" // 읽기+쓰기 (메모 작성 가능)
	PermissionOwner     RoomPermission = "OWNER"      // 소유자 (전체 권한)
)

// RoomMember 방 멤버 및 권한 관리 테이블
type RoomMember struct {
	gorm.Model
	RoomID     uint           `json:"room_id" gorm:"column:room_id;not null;index;comment:방 ID"`
	UserID     uint           `json:"user_id" gorm:"column:user_id;not null;index;comment:사용자 ID"`
	Permission RoomPermission `json:"permission" gorm:"column:permission;type:enum('READ_ONLY','READ_WRITE','OWNER');default:'READ_ONLY';index;comment:권한 레벨"`
	JoinedAt   int64          `json:"joined_at" gorm:"column:joined_at;autoCreateTime;comment:참여 시간"`
	Room       *Room          `json:"room,omitempty" gorm:"foreignKey:RoomID"`
	User       *User          `json:"user,omitempty" gorm:"foreignKey:UserID"`
}

// TableName RoomMember 테이블명 지정
func (RoomMember) TableName() string {
	return "room_members"
}

// MemoCategory 메모 카테고리 마스터 테이블
type MemoCategory struct {
	ID           uint      `json:"id" gorm:"column:id;primaryKey;autoIncrement"`
	NameKo       string    `json:"name_ko" gorm:"column:name_ko;type:varchar(100);not null;uniqueIndex;comment:카테고리 이름 (한국어)"`
	Sentiment    string    `json:"sentiment" gorm:"column:sentiment;type:enum('positive','negative','neutral');not null;index;comment:감정 분류"`
	DisplayOrder int       `json:"display_order" gorm:"column:display_order;not null;index;comment:표시 순서 (UI 정렬용)"`
	ColorHex     string    `json:"color_hex" gorm:"column:color_hex;type:varchar(7);not null;comment:UI 색상 코드 (#RRGGBB)"`
	CreatedAt    time.Time `json:"created_at" gorm:"column:created_at;autoCreateTime"`
	UpdatedAt    time.Time `json:"updated_at" gorm:"column:updated_at;autoUpdateTime"`
}

// TableName MemoCategory 테이블명 지정
func (MemoCategory) TableName() string {
	return "memo_categories"
}

// MemoCategorySelection 메모-카테고리 연결 테이블 (다대다 관계)
type MemoCategorySelection struct {
	ID         uint          `json:"id" gorm:"column:id;primaryKey;autoIncrement"`
	MemoID     uint          `json:"memo_id" gorm:"column:memo_id;not null;index;comment:메모 ID (FK)"`
	CategoryID uint          `json:"category_id" gorm:"column:category_id;not null;index;comment:카테고리 ID (FK)"`
	CreatedAt  int64         `json:"created_at" gorm:"column:created_at;autoCreateTime;comment:선택 시간"`
	UpdatedAt  int64         `json:"updated_at" gorm:"column:updated_at;autoUpdateTime;comment:수정 시간"`
	DeletedAt  *int64        `json:"deleted_at,omitempty" gorm:"column:deleted_at;index;comment:삭제 시간"`
	Memo       *Memo         `json:"memo,omitempty" gorm:"foreignKey:MemoID"`
	Category   *MemoCategory `json:"category,omitempty" gorm:"foreignKey:CategoryID"`
}

// TableName MemoCategorySelection 테이블명 지정
func (MemoCategorySelection) TableName() string {
	return "memo_category_selections"
}
