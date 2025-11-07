-- Migration: Add performance indexes
-- Date: 2025-11-07
-- Purpose: Fix slow queries on filtered searches and join operations

-- Index for category filtering joins
CREATE INDEX idx_memo_cat_sel_lookup ON memo_category_selections(category_id, memo_id, deleted_at);

-- Index for like status checks
CREATE INDEX idx_memo_likes_check ON memo_likes(memo_id, user_id);

-- Composite index for room and wishlist filtering
CREATE INDEX idx_memos_filter ON memos(room_id, is_wishlist, is_pinned, deleted_at);

-- Index for user memos with wishlist filter
CREATE INDEX idx_memos_user_wishlist ON memos(user_id, is_wishlist, deleted_at);
