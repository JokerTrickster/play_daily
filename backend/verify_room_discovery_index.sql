-- Verify Room Discovery Index Usage
-- Run these EXPLAIN queries to ensure the composite index is being used

-- 1. Check if idx_rooms_discovery index exists
SHOW INDEX FROM rooms WHERE Key_name = 'idx_rooms_discovery';

-- 2. Verify popular rooms query uses the index
-- Expected: key = 'idx_rooms_discovery', type = 'range' or 'ref'
EXPLAIN SELECT * FROM rooms
WHERE is_public = 1
ORDER BY likes_count DESC, created_at DESC
LIMIT 20;

-- 3. Verify recent rooms query uses the index
-- Expected: key = 'idx_rooms_discovery', type = 'range' or 'ref'
EXPLAIN SELECT * FROM rooms
WHERE is_public = 1
ORDER BY created_at DESC, likes_count DESC
LIMIT 20;

-- 4. Verify count query uses the index
-- Expected: key = 'idx_rooms_discovery' or at least using is_public index
EXPLAIN SELECT COUNT(*) FROM rooms WHERE is_public = 1;

-- 5. Show all indexes on rooms table
SHOW INDEX FROM rooms;
