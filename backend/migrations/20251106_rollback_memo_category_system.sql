-- Rollback: Memo Category System
-- Date: 2025-11-06
-- Issue: #41
-- Description: Rollback migration for memo category system
--
-- This rollback script:
-- 1. Drops new tables (memo_categories, memo_category_selections)
-- 2. Restores content column to memos table
-- 3. Removes creation_mode column from memos table
-- 4. Optionally restores data from backup table
--
-- WARNING: This rollback cannot restore deleted memo data unless you
--          manually restore from memos_backup_20251106 table.

-- ==============================================================================
-- PHASE 1: DROP NEW TABLES
-- ==============================================================================

-- Drop junction table first (due to foreign keys)
DROP TABLE IF EXISTS memo_category_selections;

-- Drop master table
DROP TABLE IF EXISTS memo_categories;

SELECT '✅ Dropped memo_categories and memo_category_selections tables' AS status;

-- ==============================================================================
-- PHASE 2: RESTORE MEMOS TABLE STRUCTURE
-- ==============================================================================

-- Remove creation_mode column
ALTER TABLE memos DROP COLUMN IF EXISTS creation_mode;

-- Restore content column
ALTER TABLE memos ADD COLUMN IF NOT EXISTS content TEXT
    COMMENT '메모 내용 (자유 텍스트)' AFTER title;

-- Restore original title column comment
ALTER TABLE memos MODIFY COLUMN title VARCHAR(200) NOT NULL
    COMMENT '메모 제목';

SELECT '✅ Restored memos table structure' AS status;

-- ==============================================================================
-- PHASE 3: OPTIONAL DATA RESTORATION
-- ==============================================================================

-- NOTE: Uncomment the following block if you want to restore backed-up data
-- WARNING: This will overwrite any new memos created after the migration

/*
-- Restore data from backup
INSERT INTO memos (id, user_id, title, content, image_url, rating, latitude, longitude, naver_place_url, created_at, updated_at)
SELECT id, user_id, title, content, image_url, rating, latitude, longitude, naver_place_url, created_at, updated_at
FROM memos_backup_20251106;

SELECT '✅ Restored memo data from backup' AS status,
       COUNT(*) AS restored_records
FROM memos;
*/

-- ==============================================================================
-- PHASE 4: VALIDATION
-- ==============================================================================

-- Verify table structures
SELECT 'memos table structure after rollback' AS validation_step;
DESCRIBE memos;

-- Verify new tables are gone
SELECT 'Verify tables dropped' AS validation_step;
SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('memo_categories', 'memo_category_selections');
-- Should return empty result

-- ==============================================================================
-- CLEANUP (OPTIONAL)
-- ==============================================================================

-- NOTE: Uncomment to delete backup table after successful rollback verification
-- WARNING: This permanently deletes the backup

/*
DROP TABLE IF EXISTS memos_backup_20251106;
SELECT '✅ Deleted backup table' AS status;
*/

-- ==============================================================================
-- ROLLBACK COMPLETE
-- ==============================================================================

SELECT '✅ Rollback completed successfully' AS status,
       NOW() AS completed_at,
       'Backup table (memos_backup_20251106) preserved - drop manually if no longer needed' AS note;
