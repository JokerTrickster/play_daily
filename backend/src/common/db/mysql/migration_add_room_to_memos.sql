-- Migration: Add room_id to memos table
-- Created: 2025-10-23

USE daily_dev;

-- 1. Memos 테이블에 room_id 추가 (NULL 허용)
ALTER TABLE memos
ADD COLUMN room_id BIGINT UNSIGNED NULL COMMENT '메모가 속한 방 ID' AFTER user_id,
ADD INDEX idx_room_id (room_id);

-- 2. 기존 메모들을 사용자의 기본 방에 할당
UPDATE memos m
INNER JOIN users u ON m.user_id = u.id
SET m.room_id = u.default_room_id
WHERE m.deleted_at IS NULL AND u.deleted_at IS NULL;

-- 3. room_id NOT NULL 제약 조건 추가
ALTER TABLE memos
MODIFY COLUMN room_id BIGINT UNSIGNED NOT NULL COMMENT '메모가 속한 방 ID';

-- 4. 외래키 제약 조건 추가
ALTER TABLE memos
ADD CONSTRAINT fk_memos_room_id FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE;
