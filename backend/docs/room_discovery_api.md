# Room Discovery API Documentation

## Overview
The Room Discovery API provides public endpoints for discovering available rooms without authentication. Users can browse popular rooms (sorted by likes) or recently created rooms.

## Base URL
```
/v0.1/rooms
```

## Endpoints

### 1. Get Popular Rooms

Retrieves public rooms sorted by popularity (likes count descending).

**Endpoint:** `GET /v0.1/rooms/popular`

**Authentication:** Not required (Public endpoint)

**Query Parameters:**

| Parameter | Type | Required | Default | Max | Description |
|-----------|------|----------|---------|-----|-------------|
| page | integer | No | 1 | N/A | Page number (must be >= 1) |
| limit | integer | No | 20 | 100 | Items per page (must be >= 1) |

**Success Response (200 OK):**
```json
{
  "rooms": [
    {
      "id": 1,
      "name": "Tech Discussion",
      "room_code": "TECH2024",
      "is_public": true,
      "likes_count": 150,
      "owner_id": 42,
      "created_at": "2024-11-01T10:30:00Z"
    }
  ],
  "total": 500,
  "page": 1,
  "limit": 20,
  "has_next": true
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| rooms | array | Array of room objects |
| rooms[].id | integer | Unique room identifier |
| rooms[].name | string | Room display name |
| rooms[].room_code | string | Room join code |
| rooms[].is_public | boolean | Public visibility flag (always true for discovery) |
| rooms[].likes_count | integer | Number of likes received |
| rooms[].owner_id | integer | User ID of room owner |
| rooms[].created_at | string | ISO 8601 timestamp of creation |
| total | integer | Total count of public rooms |
| page | integer | Current page number |
| limit | integer | Items per page |
| has_next | boolean | True if more pages available |

**Error Responses:**

**400 Bad Request** - Invalid parameters
```json
{
  "error": "invalid page number (must be >= 1)"
}
```
```json
{
  "error": "invalid limit (must be >= 1)"
}
```

**500 Internal Server Error** - Server error
```json
{
  "error": "database connection failed"
}
```

**Sorting:**
- Primary: `likes_count DESC` (most liked first)
- Secondary: `created_at DESC` (newer first for same like count)

---

### 2. Get Recent Rooms

Retrieves public rooms sorted by creation date (newest first).

**Endpoint:** `GET /v0.1/rooms/recent`

**Authentication:** Not required (Public endpoint)

**Query Parameters:**

| Parameter | Type | Required | Default | Max | Description |
|-----------|------|----------|---------|-----|-------------|
| page | integer | No | 1 | N/A | Page number (must be >= 1) |
| limit | integer | No | 20 | 100 | Items per page (must be >= 1) |

**Success Response (200 OK):**
```json
{
  "rooms": [
    {
      "id": 999,
      "name": "New Community",
      "room_code": "NEWC2024",
      "is_public": true,
      "likes_count": 5,
      "owner_id": 123,
      "created_at": "2024-11-11T15:00:00Z"
    }
  ],
  "total": 500,
  "page": 1,
  "limit": 20,
  "has_next": true
}
```

**Response Fields:** Same as Popular Rooms endpoint

**Error Responses:** Same as Popular Rooms endpoint

**Sorting:**
- Primary: `created_at DESC` (newest first)
- Secondary: `likes_count DESC` (more popular for same creation time)

---

## Code Examples

### cURL - Get Popular Rooms (Default)
```bash
curl -X GET "http://localhost:8080/v0.1/rooms/popular" \
  -H "Content-Type: application/json"
```

### cURL - Get Popular Rooms (Custom Pagination)
```bash
curl -X GET "http://localhost:8080/v0.1/rooms/popular?page=2&limit=10" \
  -H "Content-Type: application/json"
```

### cURL - Get Recent Rooms
```bash
curl -X GET "http://localhost:8080/v0.1/rooms/recent" \
  -H "Content-Type: application/json"
```

### cURL - Get Recent Rooms with Limit
```bash
curl -X GET "http://localhost:8080/v0.1/rooms/recent?limit=5" \
  -H "Content-Type: application/json"
```

### JavaScript (Fetch API)
```javascript
// Get popular rooms
async function getPopularRooms(page = 1, limit = 20) {
  const response = await fetch(
    `http://localhost:8080/v0.1/rooms/popular?page=${page}&limit=${limit}`
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error);
  }

  return await response.json();
}

// Get recent rooms
async function getRecentRooms(page = 1, limit = 20) {
  const response = await fetch(
    `http://localhost:8080/v0.1/rooms/recent?page=${page}&limit=${limit}`
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error);
  }

  return await response.json();
}

// Usage
try {
  const popularRooms = await getPopularRooms(1, 10);
  console.log(`Found ${popularRooms.total} popular rooms`);
  console.log(`Showing ${popularRooms.rooms.length} rooms`);
  console.log(`Has more pages: ${popularRooms.has_next}`);
} catch (error) {
  console.error('Failed to fetch rooms:', error.message);
}
```

### Go (Standard Library)
```go
package main

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
)

type RoomListResponse struct {
	Rooms   []Room `json:"rooms"`
	Total   int64  `json:"total"`
	Page    int    `json:"page"`
	Limit   int    `json:"limit"`
	HasNext bool   `json:"has_next"`
}

type Room struct {
	ID         uint   `json:"id"`
	Name       string `json:"name"`
	RoomCode   string `json:"room_code"`
	IsPublic   bool   `json:"is_public"`
	LikesCount uint   `json:"likes_count"`
	OwnerID    uint   `json:"owner_id"`
	CreatedAt  string `json:"created_at"`
}

func getPopularRooms(page, limit int) (*RoomListResponse, error) {
	baseURL := "http://localhost:8080/v0.1/rooms/popular"
	params := url.Values{}
	params.Add("page", fmt.Sprintf("%d", page))
	params.Add("limit", fmt.Sprintf("%d", limit))

	resp, err := http.Get(baseURL + "?" + params.Encode())
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != 200 {
		return nil, fmt.Errorf("API error: %s", string(body))
	}

	var result RoomListResponse
	if err := json.Unmarshal(body, &result); err != nil {
		return nil, err
	}

	return &result, nil
}
```

## Pagination Guide

### Calculating Total Pages
```javascript
const totalPages = Math.ceil(response.total / response.limit);
```

### Iterating Through All Pages
```javascript
async function getAllRooms(endpoint) {
  const allRooms = [];
  let page = 1;
  let hasNext = true;

  while (hasNext) {
    const response = await fetch(
      `http://localhost:8080/v0.1/rooms/${endpoint}?page=${page}&limit=100`
    );
    const data = await response.json();

    allRooms.push(...data.rooms);
    hasNext = data.has_next;
    page++;
  }

  return allRooms;
}
```

## Performance Considerations

### Database Indices
The API uses composite indices for optimal query performance:

**Index:** `idx_rooms_discovery`
- Columns: `(is_public, likes_count DESC, created_at DESC)`
- Type: Composite B-Tree index
- Coverage: Covers both popular and recent queries

**Expected Performance:**
- Query execution: < 10ms (with index)
- API response time: < 100ms (typical)
- API response time: < 500ms (target for 1000+ rooms)

### Optimization Tips

1. **Use Appropriate Limits**
   - Default (20): Good for pagination UI
   - Maximum (100): Minimize API calls for bulk operations
   - Small (5-10): Faster response for mobile clients

2. **Cache Results**
   - Room discovery data changes infrequently
   - Consider caching for 30-60 seconds on client side
   - Use `has_next` flag to avoid unnecessary requests

3. **Efficient Pagination**
   - Don't fetch all pages at once unless necessary
   - Use infinite scroll with incremental loading
   - Track current page in application state

## Rate Limiting

Currently, no rate limiting is applied to room discovery endpoints as they are public and read-only.

**Future Considerations:**
- May implement rate limiting if abuse detected
- Recommended: 100 requests per minute per IP
- Headers will include rate limit information if implemented

## Security Considerations

### 1. No Authentication Required
- Public endpoints accessible without tokens
- Only public rooms are returned (is_public = true)
- Private rooms are never exposed

### 2. Input Validation
- Page number must be >= 1
- Limit must be >= 1
- Limit is automatically capped at 100
- Non-numeric parameters return 400 Bad Request

### 3. Data Exposure
- Only safe fields are returned (no passwords, sensitive data)
- Room passwords are never included in responses
- User personal information is not exposed (only owner_id)

### 4. SQL Injection Protection
- All queries use parameterized statements via GORM
- No raw SQL concatenation
- Input sanitization handled by framework

### 5. No Modification Operations
- GET requests only (read-only)
- No state changes possible through these endpoints
- Room likes require separate authenticated endpoint

## Common Use Cases

### 1. Room Discovery Screen
Display popular and recent rooms in separate tabs:
```javascript
const [popularRooms, setPopularRooms] = useState([]);
const [recentRooms, setRecentRooms] = useState([]);

useEffect(() => {
  fetch('/v0.1/rooms/popular?limit=10')
    .then(res => res.json())
    .then(data => setPopularRooms(data.rooms));

  fetch('/v0.1/rooms/recent?limit=10')
    .then(res => res.json())
    .then(data => setRecentRooms(data.rooms));
}, []);
```

### 2. Infinite Scroll
Load more rooms as user scrolls:
```javascript
const [rooms, setRooms] = useState([]);
const [page, setPage] = useState(1);
const [hasNext, setHasNext] = useState(true);

const loadMore = async () => {
  if (!hasNext) return;

  const response = await fetch(`/v0.1/rooms/popular?page=${page}&limit=20`);
  const data = await response.json();

  setRooms([...rooms, ...data.rooms]);
  setHasNext(data.has_next);
  setPage(page + 1);
};
```

### 3. Search Results Pagination
Display paginated search results:
```javascript
const [currentPage, setCurrentPage] = useState(1);
const [totalPages, setTotalPages] = useState(0);

const fetchPage = async (pageNum) => {
  const response = await fetch(`/v0.1/rooms/popular?page=${pageNum}&limit=20`);
  const data = await response.json();

  setTotalPages(Math.ceil(data.total / data.limit));
  return data.rooms;
};
```

## Related Endpoints

- **POST /v0.1/room/join** - Join a discovered room (requires authentication)
- **POST /v0.1/rooms/:id/like** - Like a room (requires authentication)
- **POST /v0.1/rooms/:id/reset-password** - Reset room password (requires authentication and ownership)

## Changelog

### v0.1 (November 2024)
- Initial release of room discovery endpoints
- Popular rooms endpoint (GET /v0.1/rooms/popular)
- Recent rooms endpoint (GET /v0.1/rooms/recent)
- Pagination support with has_next flag
- Composite database index for performance
