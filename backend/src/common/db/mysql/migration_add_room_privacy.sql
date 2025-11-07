-- Migration: Add privacy control fields to rooms table
-- Issue: #51 - Database Migration for Room Privacy
-- Created: 2025-11-07

-- ============================================================
-- MIGRATION UP: Add room privacy fields
-- ============================================================

-- Add is_public flag (default: false for existing rooms - private by default)
ALTER TABLE rooms
ADD COLUMN is_public BOOLEAN DEFAULT false NOT NULL
COMMENT 'Room visibility (true=public, false=private)' AFTER owner_user_id;

-- Add password field for private rooms
ALTER TABLE rooms
ADD COLUMN password VARCHAR(4) NULL
COMMENT '4-digit room password (NULL for public rooms)' AFTER is_public;

-- Create composite index for efficient room discovery queries
-- This supports queries like: SELECT * FROM rooms WHERE is_public = true ORDER BY likes_count DESC
CREATE INDEX idx_room_discovery ON rooms(is_public, likes_count, deleted_at);

-- Backfill existing rooms with user's room password (for backward compatibility)
-- All existing rooms will be set as private with the owner's room password
UPDATE rooms r
INNER JOIN users u ON u.default_room_id = r.id
SET r.password = u.room_password,
    r.is_public = false
WHERE r.password IS NULL;

-- Add index for efficient member validation during like operations
-- This supports queries like: SELECT * FROM room_members WHERE room_id = ? AND user_id = ? AND deleted_at IS NULL
CREATE INDEX idx_room_members_validation ON room_members(room_id, user_id, deleted_at);

-- ============================================================
-- MIGRATION DOWN: Remove privacy fields (ROLLBACK)
-- ============================================================

/*
-- Run these commands to rollback this migration:

DROP INDEX IF EXISTS idx_room_members_validation ON room_members;
DROP INDEX IF EXISTS idx_room_discovery ON rooms;

ALTER TABLE rooms DROP COLUMN IF EXISTS password;
ALTER TABLE rooms DROP COLUMN IF EXISTS is_public;
*/

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================

/*
-- Verify schema changes:
DESC rooms;

-- Verify index creation:
SHOW INDEX FROM rooms WHERE Key_name = 'idx_room_discovery';
SHOW INDEX FROM room_members WHERE Key_name = 'idx_room_members_validation';

-- Verify existing data integrity:
SELECT COUNT(*) as total_rooms,
       SUM(CASE WHEN is_public = true THEN 1 ELSE 0 END) as public_rooms,
       SUM(CASE WHEN is_public = false THEN 1 ELSE 0 END) as private_rooms,
       SUM(CASE WHEN password IS NOT NULL THEN 1 ELSE 0 END) as rooms_with_password,
       SUM(CASE WHEN password IS NULL THEN 1 ELSE 0 END) as rooms_without_password
FROM rooms;

-- Verify all existing rooms have passwords:
SELECT id, room_code, name, is_public,
       CASE WHEN password IS NOT NULL THEN 'YES' ELSE 'NO' END as has_password
FROM rooms
WHERE password IS NULL;

-- Test query performance with new index (public room discovery):
EXPLAIN SELECT * FROM rooms
WHERE is_public = true AND deleted_at IS NULL
ORDER BY likes_count DESC
LIMIT 10;

-- Test query performance with member validation index:
EXPLAIN SELECT EXISTS(
  SELECT 1 FROM room_members
  WHERE room_id = 1 AND user_id = 1 AND deleted_at IS NULL
) as is_member;
*/
