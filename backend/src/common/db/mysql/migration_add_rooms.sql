-- Migration: Add Room system
-- Created: 2025-10-23
-- Description: Add rooms table and link users/memos to rooms

USE daily_dev;

-- 1. Rooms Table: 방 정보 관리
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    room_code VARCHAR(50) NOT NULL UNIQUE COMMENT '방 고유 코드 (UUID)',
    name VARCHAR(100) NOT NULL COMMENT '방 이름',
    owner_user_id BIGINT UNSIGNED NOT NULL COMMENT '방 소유자 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    deleted_at TIMESTAMP NULL DEFAULT NULL COMMENT '삭제 시간 (soft delete)',
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_room_code (room_code),
    INDEX idx_owner_user_id (owner_user_id),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='방 정보 테이블';

-- 2. Users 테이블에 default_room_id 추가
ALTER TABLE users
ADD COLUMN default_room_id BIGINT UNSIGNED NULL COMMENT '기본 방 ID (회원가입 시 자동 생성된 방)' AFTER nickname,
ADD INDEX idx_default_room_id (default_room_id);

-- 3. Memos 테이블에 room_id 추가 (외래키 없이 먼저 추가)
ALTER TABLE memos
ADD COLUMN room_id BIGINT UNSIGNED NULL COMMENT '메모가 속한 방 ID' AFTER user_id,
ADD INDEX idx_room_id (room_id);

-- 4. 기존 사용자들을 위한 기본 방 생성 및 매핑
-- 기존 사용자마다 개인 방 생성
INSERT INTO rooms (room_code, name, owner_user_id, created_at, updated_at)
SELECT
    UUID() as room_code,
    CONCAT(nickname, '의 방') as name,
    id as owner_user_id,
    created_at,
    updated_at
FROM users
WHERE deleted_at IS NULL;

-- 사용자의 default_room_id 업데이트
UPDATE users u
INNER JOIN rooms r ON r.owner_user_id = u.id
SET u.default_room_id = r.id
WHERE u.deleted_at IS NULL;

-- 기존 메모들을 사용자의 기본 방에 할당
UPDATE memos m
INNER JOIN users u ON m.user_id = u.id
SET m.room_id = u.default_room_id
WHERE m.deleted_at IS NULL AND u.deleted_at IS NULL;

-- 5. room_id NOT NULL 제약 조건 추가
ALTER TABLE memos
MODIFY COLUMN room_id BIGINT UNSIGNED NOT NULL COMMENT '메모가 속한 방 ID';

-- 6. 외래키 제약 조건 추가
ALTER TABLE memos
ADD CONSTRAINT fk_memos_room_id FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE;
