# Room Discovery API - Quick Start Guide

## Quick Test Commands

### 1. Get Popular Rooms (Default)
```bash
curl http://localhost:8080/v0.1/rooms/popular | jq
```

### 2. Get Popular Rooms (Page 1, 5 items)
```bash
curl "http://localhost:8080/v0.1/rooms/popular?page=1&limit=5" | jq
```

### 3. Get Recent Rooms (Default)
```bash
curl http://localhost:8080/v0.1/rooms/recent | jq
```

### 4. Get Recent Rooms (Page 2, 10 items)
```bash
curl "http://localhost:8080/v0.1/rooms/recent?page=2&limit=10" | jq
```

## Expected Response Format
```json
{
  "rooms": [
    {
      "id": 123,
      "name": "My Public Room",
      "room_code": "550e8400-e29b-41d4-a716-446655440000",
      "is_public": true,
      "likes_count": 42,
      "owner_id": 5,
      "created_at": "2025-11-11T10:30:00Z"
    }
  ],
  "total": 100,
  "page": 1,
  "limit": 20,
  "has_next": true
}
```

## Parameter Validation

### Valid Parameters
- `page`: Integer >= 1 (default: 1)
- `limit`: Integer 1-100 (default: 20, max: 100)

### Error Responses

**Invalid page:**
```bash
curl "http://localhost:8080/v0.1/rooms/popular?page=0"
# Response: {"error":"invalid page number (must be >= 1)"}
```

**Invalid limit:**
```bash
curl "http://localhost:8080/v0.1/rooms/popular?limit=-5"
# Response: {"error":"invalid limit (must be >= 1)"}
```

**Limit auto-capped:**
```bash
curl "http://localhost:8080/v0.1/rooms/popular?limit=200"
# Limit automatically capped to 100
```

## Database Setup

### 1. Apply Migration
```bash
mysql -u your_user -p your_database < src/common/db/mysql/migration_room_discovery_index.sql
```

### 2. Verify Index
```bash
mysql -u your_user -p your_database < verify_room_discovery_index.sql
```

### 3. Check Index Exists
```sql
SHOW INDEX FROM rooms WHERE Key_name = 'idx_rooms_discovery';
```

## Performance Verification

### 1. Explain Popular Query
```sql
EXPLAIN SELECT * FROM rooms
WHERE is_public = 1
ORDER BY likes_count DESC, created_at DESC
LIMIT 20;
```
Expected: `key = 'idx_rooms_discovery'`

### 2. Explain Recent Query
```sql
EXPLAIN SELECT * FROM rooms
WHERE is_public = 1
ORDER BY created_at DESC, likes_count DESC
LIMIT 20;
```
Expected: `key = 'idx_rooms_discovery'`

## Acceptance Criteria Checklist

- [x] GET /v0.1/rooms/popular endpoint implemented
- [x] GET /v0.1/rooms/recent endpoint implemented
- [x] Pagination with metadata (total, page, limit, has_next)
- [x] Parameter validation with error messages
- [x] Using composite index (idx_rooms_discovery)
- [x] Following existing API patterns (memo list)
- [x] Response model excludes sensitive data
- [x] No authentication required (public discovery)
- [x] Target response time < 100ms (with index)

## Files Modified/Created

### Created Files (8)
1. `/src/features/room/model/response/roomList.go`
2. `/src/features/room/model/interface/IRoomDiscoveryRepository.go`
3. `/src/features/room/model/interface/IRoomDiscoveryUseCase.go`
4. `/src/features/room/model/interface/IRoomDiscoveryHandler.go`
5. `/src/features/room/repository/roomDiscoveryRepository.go`
6. `/src/features/room/usecase/roomDiscoveryUseCase.go`
7. `/src/features/room/handler/roomDiscoveryHandler.go`
8. `/src/common/db/mysql/migration_room_discovery_index.sql`

### Modified Files (1)
1. `/src/features/room/handler/index.go` - Added discovery handler registration

## Integration Notes

- Endpoints are public (no TokenChecker middleware)
- Uses existing Room model from gormDB.go
- Compatible with existing room management endpoints
- Ready for frontend pagination component integration
- Follows same patterns as memo list API
