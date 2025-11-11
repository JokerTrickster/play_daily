-- Migration: Add Room Discovery Indices and Password Reset Audit
-- Created: 2025-11-11
-- Description: Add composite indices for efficient room discovery (popular/recent) and audit table for password resets
-- Related: GitHub Issue #60 - Database Setup and Indexing for room-search-discovery epic

USE daily_dev;

-- ====================================================================
-- PART 1: Room Discovery Performance Indices
-- ====================================================================

-- Index for popular rooms query: ORDER BY likes_count DESC, created_at DESC
-- Use case: Finding trending/popular rooms with recency as tie-breaker
-- Query pattern: SELECT * FROM rooms WHERE is_public = 1 AND deleted_at IS NULL ORDER BY likes_count DESC, created_at DESC LIMIT 20
-- Idempotent: Check if index exists before creating
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
               WHERE table_schema = DATABASE() AND table_name = 'rooms' AND index_name = 'idx_rooms_popular');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_rooms_popular already exists'' AS Status',
                   'CREATE INDEX idx_rooms_popular ON rooms(likes_count DESC, created_at DESC)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index for recent rooms query: ORDER BY created_at DESC, likes_count DESC
-- Use case: Finding newly created rooms with popularity as secondary sort
-- Query pattern: SELECT * FROM rooms WHERE is_public = 1 AND deleted_at IS NULL ORDER BY created_at DESC, likes_count DESC LIMIT 20
-- Idempotent: Check if index exists before creating
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
               WHERE table_schema = DATABASE() AND table_name = 'rooms' AND index_name = 'idx_rooms_recent');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_rooms_recent already exists'' AS Status',
                   'CREATE INDEX idx_rooms_recent ON rooms(created_at DESC, likes_count DESC)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Composite index for filtered room discovery with public status
-- Use case: Efficient filtering of public rooms during discovery queries
-- Query pattern: SELECT * FROM rooms WHERE is_public = 1 AND deleted_at IS NULL ORDER BY likes_count DESC
-- Idempotent: Check if index exists before creating
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
               WHERE table_schema = DATABASE() AND table_name = 'rooms' AND index_name = 'idx_rooms_public_discovery');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_rooms_public_discovery already exists'' AS Status',
                   'CREATE INDEX idx_rooms_public_discovery ON rooms(is_public, deleted_at, likes_count DESC)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ====================================================================
-- PART 2: Room Password Reset Audit Table
-- ====================================================================

-- Create room_password_resets table for security audit trail
-- Purpose: Track all password reset operations for compliance and security monitoring
CREATE TABLE IF NOT EXISTS room_password_resets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT UNSIGNED NOT NULL COMMENT '비밀번호가 변경된 방 ID',
    reset_by_user_id BIGINT UNSIGNED NULL COMMENT '비밀번호를 변경한 사용자 ID (방 소유자, NULL if user deleted)',
    reset_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '비밀번호 변경 시간',
    previous_password_hash VARCHAR(255) NOT NULL COMMENT '이전 비밀번호 해시값 (복구용)',
    new_password_hash VARCHAR(255) NOT NULL COMMENT '새 비밀번호 해시값',
    ip_address VARCHAR(45) NULL COMMENT '요청 IP 주소 (IPv4/IPv6)',
    user_agent VARCHAR(500) NULL COMMENT '사용자 브라우저/클라이언트 정보',

    -- Foreign key constraints
    CONSTRAINT fk_room_password_resets_room_id
        FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_room_password_resets_user_id
        FOREIGN KEY (reset_by_user_id) REFERENCES users(id) ON DELETE SET NULL,

    -- Performance indices
    INDEX idx_room_password_resets_room_id (room_id) COMMENT 'Room lookup for audit history',
    INDEX idx_room_password_resets_reset_at (reset_at DESC) COMMENT 'Time-based audit queries',
    INDEX idx_room_password_resets_user_id (reset_by_user_id) COMMENT 'User activity tracking'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='방 비밀번호 변경 감사 로그 테이블';


-- ====================================================================
-- VERIFICATION QUERIES (for testing - comment out in production)
-- ====================================================================

-- Verify indices were created
-- SHOW INDEX FROM rooms WHERE Key_name LIKE 'idx_rooms_%';

-- Verify audit table was created
-- SHOW CREATE TABLE room_password_resets;

-- Test popular rooms query with EXPLAIN
-- EXPLAIN SELECT * FROM rooms WHERE is_public = 1 AND deleted_at IS NULL ORDER BY likes_count DESC, created_at DESC LIMIT 20;

-- Test recent rooms query with EXPLAIN
-- EXPLAIN SELECT * FROM rooms WHERE is_public = 1 AND deleted_at IS NULL ORDER BY created_at DESC, likes_count DESC LIMIT 20;
