-- Migration: Add Room Likes system
-- Created: 2025-10-30
-- Description: Add room_likes table for users to like rooms

USE daily_dev;

-- Room Likes Table: 방 좋아요 정보 관리
CREATE TABLE IF NOT EXISTS room_likes (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT UNSIGNED NOT NULL COMMENT '좋아요한 방 ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '좋아요를 누른 사용자 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요 누른 시간',
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_room_like (room_id, user_id) COMMENT '방당 사용자당 한 번만 좋아요 가능',
    INDEX idx_room_id (room_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='방 좋아요 정보 테이블';

-- Rooms 테이블에 좋아요 개수 컬럼 추가 (성능 최적화용)
ALTER TABLE rooms
ADD COLUMN likes_count INT UNSIGNED DEFAULT 0 COMMENT '좋아요 개수' AFTER name,
ADD INDEX idx_likes_count (likes_count);
