-- Migration: Add room discovery composite index
-- Date: 2025-11-11
-- Purpose: Optimize room discovery queries for popular and recent sorting
-- Related: GitHub Issue #60, #62

-- Composite index for room discovery endpoints
-- Supports both popular (likes DESC, created_at DESC) and recent (created_at DESC, likes DESC) queries
CREATE INDEX idx_rooms_discovery ON rooms(is_public, likes_count DESC, created_at DESC);

-- This index optimally supports:
-- 1. WHERE is_public = 1 ORDER BY likes_count DESC, created_at DESC (Popular rooms)
-- 2. WHERE is_public = 1 ORDER BY created_at DESC, likes_count DESC (Recent rooms)
