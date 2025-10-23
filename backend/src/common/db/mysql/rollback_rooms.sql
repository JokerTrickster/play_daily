-- Rollback: Remove Room system
-- Created: 2025-10-23

USE daily_dev;

-- 1. Memos 테이블에서 room_id 제거
ALTER TABLE memos
DROP FOREIGN KEY memos_ibfk_2,  -- room_id foreign key (이름은 실제 환경에 따라 다를 수 있음)
DROP INDEX idx_room_id,
DROP COLUMN room_id;

-- 2. Users 테이블에서 default_room_id 제거
ALTER TABLE users
DROP INDEX idx_default_room_id,
DROP COLUMN default_room_id;

-- 3. Rooms 테이블 삭제
DROP TABLE IF EXISTS rooms;
