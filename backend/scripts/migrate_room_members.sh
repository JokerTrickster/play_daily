#!/bin/bash

# Migration script for room_members table
# Usage: ./migrate_room_members.sh

set -e

DB_HOST="${DB_HOST:-13.203.37.93}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-examplepassword}"
DB_NAME="${DB_NAME:-daily_dev}"

echo "Starting room_members table migration..."
echo "Target: $DB_USER@$DB_HOST:$DB_PORT/$DB_NAME"

# Execute migration SQL
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" << 'EOF'
-- Migration: Add Room Members with Permission System
-- Created: 2025-11-05
-- Description: Add room_members table to manage user permissions in rooms

-- 1. Room Members Table: 방 멤버 및 권한 관리
CREATE TABLE IF NOT EXISTS room_members (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT UNSIGNED NOT NULL COMMENT '방 ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID',
    permission ENUM('READ_ONLY', 'READ_WRITE', 'OWNER') NOT NULL DEFAULT 'READ_ONLY' COMMENT '권한 레벨 (읽기전용, 읽기+쓰기, 소유자)',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '참여 시간',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    deleted_at TIMESTAMP NULL DEFAULT NULL COMMENT '추방/탈퇴 시간 (soft delete)',
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_room_user (room_id, user_id, deleted_at),
    INDEX idx_room_id (room_id),
    INDEX idx_user_id (user_id),
    INDEX idx_permission (permission),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='방 멤버 및 권한 관리 테이블';

-- 2. 기존 방 소유자들을 room_members에 OWNER 권한으로 추가
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
ON DUPLICATE KEY UPDATE permission = 'OWNER';

-- 3. 현재 방에 참여한 사용자들을 room_members에 추가 (joinRoom API를 통해 참여한 사용자)
-- 이미 다른 사람의 방에 메모를 작성한 경우 READ_WRITE 권한 부여
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
GROUP BY m.room_id, m.user_id;

-- Verify migration
SELECT COUNT(*) as total_members FROM room_members;
SELECT permission, COUNT(*) as count FROM room_members GROUP BY permission;

EOF

echo "Migration completed successfully!"
echo "Verifying room_members table..."

mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "DESCRIBE room_members;"

echo "Done!"
