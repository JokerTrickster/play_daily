-- Rollback Migration: Remove wishlist support fields from memos table
-- Issue: #19 - Database Schema Migration - Wishlist Support
-- Created: 2025-10-21
--
-- This script reverts changes made by migration_add_wishlist_fields.sql

-- ============================================================
-- ROLLBACK: Remove wishlist fields
-- ============================================================

-- Remove index
DROP INDEX IF EXISTS idx_user_wishlist ON memos;

-- Remove business information fields (in reverse order of creation)
ALTER TABLE memos DROP COLUMN IF EXISTS business_address;
ALTER TABLE memos DROP COLUMN IF EXISTS business_phone;
ALTER TABLE memos DROP COLUMN IF EXISTS business_name;

-- Remove wishlist flag
ALTER TABLE memos DROP COLUMN IF EXISTS is_wishlist;

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================

/*
-- Verify rollback completion:
DESC memos;

-- Verify index removal:
SHOW INDEX FROM memos WHERE Key_name = 'idx_user_wishlist';
-- Should return empty result

-- Verify existing memo data is intact:
SELECT COUNT(*) as total_memos FROM memos;
*/
