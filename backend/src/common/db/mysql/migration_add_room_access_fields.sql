-- Migration: Add room access control fields
-- Created: 2025-01-10
-- Description: Add room_password and is_public fields to rooms table for access control

USE daily_dev;

-- 1. Add room_password field (4-digit password for joining room)
ALTER TABLE rooms
ADD COLUMN room_password VARCHAR(4) NOT NULL DEFAULT '0000' COMMENT '방 입장 비밀번호 (4자리)' AFTER name,
ADD INDEX idx_room_password (room_password);

-- 2. Add is_public field (public rooms don't require password)
ALTER TABLE rooms
ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 0 COMMENT '공개 방 여부 (1=공개, 0=비공개)' AFTER room_password,
ADD INDEX idx_is_public (is_public);
