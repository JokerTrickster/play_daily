-- Migration: Add bio field to users table
-- Created: 2025-11-11
-- Description: Add self-introduction/bio field for user profiles

USE daily_dev;

-- Add bio column to users table
ALTER TABLE users
ADD COLUMN bio VARCHAR(500) NULL COMMENT '사용자 자기소개' AFTER profile_image_url;
