package handler

import (
	"main/common/db/mysql"
	"net/http"

	"github.com/labstack/echo/v4"
	"gorm.io/gorm"
)

type MigrationHandler struct {
	db *gorm.DB
}

func NewMigrationHandler(db *gorm.DB) *MigrationHandler {
	return &MigrationHandler{db: db}
}

// RunRoomMembersMigration executes the room_members table migration
func (h *MigrationHandler) RunRoomMembersMigration(c echo.Context) error {
	// Security: Only allow in development mode
	// In production, remove this endpoint or add proper authentication

	// Step 1: Create room_members table
	sqlCreate := `
	CREATE TABLE IF NOT EXISTS room_members (
		id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
		room_id BIGINT UNSIGNED NOT NULL COMMENT '방 ID',
		user_id BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID',
		permission ENUM('READ_ONLY', 'READ_WRITE', 'OWNER') NOT NULL DEFAULT 'READ_ONLY' COMMENT '권한 레벨',
		joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '참여 시간',
		created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
		updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
		deleted_at TIMESTAMP NULL DEFAULT NULL COMMENT '추방/탈퇴 시간',
		FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
		FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
		UNIQUE KEY unique_room_user (room_id, user_id, deleted_at),
		INDEX idx_room_id (room_id),
		INDEX idx_user_id (user_id),
		INDEX idx_permission (permission),
		INDEX idx_deleted_at (deleted_at)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='방 멤버 및 권한 관리 테이블'
	`

	if err := h.db.Exec(sqlCreate).Error; err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]interface{}{
			"error":   "Failed to create table",
			"details": err.Error(),
		})
	}

	// Step 2: Insert existing room owners
	sqlInsertOwners := `
	INSERT INTO room_members (room_id, user_id, permission, joined_at, created_at, updated_at)
	SELECT
		r.id as room_id,
		r.owner_user_id as user_id,
		'OWNER' as permission,
		r.created_at as joined_at,
		r.created_at,
		r.updated_at
	FROM rooms r
	WHERE r.deleted_at IS NULL
	ON DUPLICATE KEY UPDATE permission = 'OWNER'
	`

	if err := h.db.Exec(sqlInsertOwners).Error; err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]interface{}{
			"error":   "Failed to insert owners",
			"details": err.Error(),
		})
	}

	// Step 3: Insert existing memo authors as READ_WRITE members
	sqlInsertMembers := `
	INSERT IGNORE INTO room_members (room_id, user_id, permission, joined_at, created_at, updated_at)
	SELECT DISTINCT
		m.room_id,
		m.user_id,
		'READ_WRITE' as permission,
		MIN(m.created_at) as joined_at,
		MIN(m.created_at) as created_at,
		NOW() as updated_at
	FROM memos m
	INNER JOIN rooms r ON m.room_id = r.id
	WHERE m.deleted_at IS NULL
		AND r.deleted_at IS NULL
		AND m.user_id != r.owner_user_id
	GROUP BY m.room_id, m.user_id
	`

	if err := h.db.Exec(sqlInsertMembers).Error; err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]interface{}{
			"error":   "Failed to insert members",
			"details": err.Error(),
		})
	}

	// Step 4: Verify migration
	var totalMembers int64
	h.db.Model(&mysql.RoomMember{}).Count(&totalMembers)

	type PermissionCount struct {
		Permission string
		Count      int64
	}
	var permissionCounts []PermissionCount
	h.db.Model(&mysql.RoomMember{}).
		Select("permission, COUNT(*) as count").
		Group("permission").
		Scan(&permissionCounts)

	return c.JSON(http.StatusOK, map[string]interface{}{
		"message":           "Migration completed successfully",
		"total_members":     totalMembers,
		"permission_counts": permissionCounts,
	})
}
