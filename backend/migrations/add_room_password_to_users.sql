-- Add room_password column to users table
-- Migration: add_room_password_to_users
-- Date: 2025-01-31

ALTER TABLE users
ADD COLUMN room_password VARCHAR(4) NOT NULL DEFAULT '0000' COMMENT '방 비밀번호 (4자리 숫자)'
AFTER default_room_id;

-- Update existing users with random 4-digit passwords
UPDATE users
SET room_password = LPAD(FLOOR(RAND() * 10000), 4, '0')
WHERE room_password = '0000';
