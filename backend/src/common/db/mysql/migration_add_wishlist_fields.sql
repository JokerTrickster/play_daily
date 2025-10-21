-- Migration: Add wishlist support fields to memos table
-- Issue: #19 - Database Schema Migration - Wishlist Support
-- Created: 2025-10-21

-- ============================================================
-- MIGRATION UP: Add wishlist fields
-- ============================================================

-- Add is_wishlist flag (default: false for existing memos)
ALTER TABLE memos
ADD COLUMN is_wishlist BOOLEAN DEFAULT false NOT NULL
COMMENT 'True if this is a wishlist item (want to visit), false if visited';

-- Add business information fields (optional, can be null)
ALTER TABLE memos
ADD COLUMN business_name VARCHAR(255)
COMMENT 'Name of the business/place from Kakao/Naver';

ALTER TABLE memos
ADD COLUMN business_phone VARCHAR(50)
COMMENT 'Contact phone number for the business';

ALTER TABLE memos
ADD COLUMN business_address TEXT
COMMENT 'Full address of the business';

-- Create composite index for efficient room-based wishlist queries
-- This supports queries like: SELECT * FROM memos WHERE user_id = ? AND is_wishlist = ?
CREATE INDEX idx_user_wishlist ON memos(user_id, is_wishlist);

-- ============================================================
-- MIGRATION DOWN: Remove wishlist fields (ROLLBACK)
-- ============================================================

/*
-- Run these commands to rollback this migration:

DROP INDEX IF EXISTS idx_user_wishlist ON memos;

ALTER TABLE memos DROP COLUMN IF EXISTS business_address;
ALTER TABLE memos DROP COLUMN IF EXISTS business_phone;
ALTER TABLE memos DROP COLUMN IF EXISTS business_name;
ALTER TABLE memos DROP COLUMN IF EXISTS is_wishlist;
*/

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================

/*
-- Verify schema changes:
DESC memos;

-- Verify index creation:
SHOW INDEX FROM memos WHERE Key_name = 'idx_user_wishlist';

-- Verify existing data integrity:
SELECT COUNT(*) as total_memos,
       SUM(CASE WHEN is_wishlist = true THEN 1 ELSE 0 END) as wishlists,
       SUM(CASE WHEN is_wishlist = false THEN 1 ELSE 0 END) as visited
FROM memos;

-- Test query performance with new index:
EXPLAIN SELECT * FROM memos WHERE user_id = 1 AND is_wishlist = false;
*/
