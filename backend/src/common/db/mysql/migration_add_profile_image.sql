-- Migration: Add profile_image_url to users table
-- Created: 2025-10-23
-- Issue: #30

USE daily_dev;

-- Add profile_image_url column to users table
ALTER TABLE users
ADD COLUMN profile_image_url VARCHAR(500) NULL COMMENT '프로필 이미지 URL' AFTER nickname,
ADD INDEX idx_profile_image_url (profile_image_url);
