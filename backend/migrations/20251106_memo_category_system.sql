-- Migration: Memo Category System
-- Date: 2025-11-06
-- Issue: #41
-- Description: Replace free-text memo content with multi-select category system
--
-- This migration:
-- 1. Backs up existing memos table
-- 2. Truncates all memo data (as per product requirement)
-- 3. Creates memo_categories and memo_category_selections tables
-- 4. Alters memos table (removes content column, adds creation_mode)
-- 5. Seeds 10 predefined sentiment categories
--
-- WARNING: This migration deletes all existing memo data.
--          Backup table is created for safety but data will not be restored.

-- ==============================================================================
-- PHASE 1: BACKUP
-- ==============================================================================

CREATE TABLE IF NOT EXISTS memos_backup_20251106 AS SELECT * FROM memos;

-- Verify backup was created
SELECT
    'Backup created' AS status,
    COUNT(*) AS records_backed_up
FROM memos_backup_20251106;

-- ==============================================================================
-- PHASE 2: TRUNCATE EXISTING DATA
-- ==============================================================================

TRUNCATE TABLE memos;

-- Verify truncation
SELECT
    'Memos truncated' AS status,
    COUNT(*) AS remaining_records
FROM memos;

-- ==============================================================================
-- PHASE 3: CREATE NEW TABLES
-- ==============================================================================

-- Create memo_categories master table
CREATE TABLE IF NOT EXISTS memo_categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name_ko VARCHAR(100) NOT NULL COMMENT '카테고리 이름 (한국어)',
    sentiment ENUM('positive', 'negative', 'neutral') NOT NULL COMMENT '감정 분류',
    display_order INT NOT NULL COMMENT '표시 순서 (UI 정렬용)',
    color_hex VARCHAR(7) NOT NULL COMMENT 'UI 색상 코드 (#RRGGBB)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name_ko (name_ko),
    KEY idx_sentiment (sentiment),
    KEY idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메모 카테고리 마스터 테이블 (제품 정의 고정 값)';

-- Create memo_category_selections junction table
CREATE TABLE IF NOT EXISTS memo_category_selections (
    id INT PRIMARY KEY AUTO_INCREMENT,
    memo_id INT NOT NULL COMMENT '메모 ID (FK)',
    category_id INT NOT NULL COMMENT '카테고리 ID (FK)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_memo_category (memo_id, category_id) COMMENT '중복 선택 방지',
    FOREIGN KEY (memo_id) REFERENCES memos(id) ON DELETE CASCADE COMMENT '메모 삭제 시 연관 삭제',
    FOREIGN KEY (category_id) REFERENCES memo_categories(id) ON DELETE RESTRICT COMMENT '카테고리 삭제 방지',
    KEY idx_memo_id (memo_id),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메모-카테고리 연결 테이블 (다대다 관계)';

-- ==============================================================================
-- PHASE 4: ALTER MEMOS TABLE
-- ==============================================================================

-- Remove content column (no longer needed)
ALTER TABLE memos DROP COLUMN IF EXISTS content;

-- Add creation_mode enum column
ALTER TABLE memos ADD COLUMN IF NOT EXISTS creation_mode ENUM('map', 'list') NOT NULL DEFAULT 'list'
    COMMENT '생성 방식 (map: 지도 기반 자동 제목, list: 사용자 입력 제목)' AFTER title;

-- Update title column comment for clarity
ALTER TABLE memos MODIFY COLUMN title VARCHAR(200) NOT NULL
    COMMENT '메모 제목 (지도 선택 시 장소명, 리스트 생성 시 사용자 입력)';

-- ==============================================================================
-- PHASE 5: SEED CATEGORIES
-- ==============================================================================

-- Insert 10 predefined sentiment categories
INSERT INTO memo_categories (name_ko, sentiment, display_order, color_hex) VALUES
-- Positive categories (5개)
('너무 좋았다', 'positive', 1, '#10B981'),
('다음에 또 방문할 의향이 있다', 'positive', 2, '#10B981'),
('가성비 좋다', 'positive', 3, '#10B981'),
('분위기가 좋다', 'positive', 4, '#10B981'),
('음식/서비스가 훌륭하다', 'positive', 5, '#10B981'),

-- Negative categories (3개)
('완전 최악, 가성비 최악', 'negative', 6, '#EF4444'),
('다시는 안 갈 것 같다', 'negative', 7, '#EF4444'),
('기대 이하였다', 'negative', 8, '#EF4444'),

-- Neutral categories (2개)
('그냥 무난했다', 'neutral', 9, '#6B7280'),
('특별한 점이 없었다', 'neutral', 10, '#6B7280');

-- ==============================================================================
-- PHASE 6: VALIDATION
-- ==============================================================================

-- Verify table structures
SELECT 'memos table structure' AS validation_step;
DESCRIBE memos;

SELECT 'memo_categories table structure' AS validation_step;
DESCRIBE memo_categories;

SELECT 'memo_category_selections table structure' AS validation_step;
DESCRIBE memo_category_selections;

-- Verify category data
SELECT 'Category seed data' AS validation_step;
SELECT
    id,
    name_ko,
    sentiment,
    display_order,
    color_hex
FROM memo_categories
ORDER BY display_order;

-- Verify category count
SELECT
    'Category count validation' AS status,
    COUNT(*) AS total_categories,
    SUM(CASE WHEN sentiment = 'positive' THEN 1 ELSE 0 END) AS positive_count,
    SUM(CASE WHEN sentiment = 'negative' THEN 1 ELSE 0 END) AS negative_count,
    SUM(CASE WHEN sentiment = 'neutral' THEN 1 ELSE 0 END) AS neutral_count
FROM memo_categories;

-- Verify memos table is empty
SELECT
    'Memos table validation' AS status,
    COUNT(*) AS memo_count
FROM memos;

-- Verify foreign key constraints exist
SELECT
    'Foreign key validation' AS status,
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'memo_category_selections'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ==============================================================================
-- MIGRATION COMPLETE
-- ==============================================================================

SELECT '✅ Migration completed successfully' AS status,
       NOW() AS completed_at;
