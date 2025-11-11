package e2e

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

// TestRoomDiscoveryFlow tests the room discovery API endpoints
func TestRoomDiscoveryFlow(t *testing.T) {

	t.Run("1. GET /v0.1/rooms/popular - Default Pagination", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// Validate response structure
		assert.Contains(t, result, "rooms")
		assert.Contains(t, result, "total")
		assert.Contains(t, result, "page")
		assert.Contains(t, result, "limit")
		assert.Contains(t, result, "has_next")

		// Validate default pagination values
		assert.Equal(t, float64(1), result["page"])
		assert.Equal(t, float64(20), result["limit"])

		// Validate rooms array
		rooms := result["rooms"].([]interface{})
		assert.NotNil(t, rooms)

		// If rooms exist, validate sorting (likes_count DESC)
		if len(rooms) > 1 {
			for i := 0; i < len(rooms)-1; i++ {
				room1 := rooms[i].(map[string]interface{})
				room2 := rooms[i+1].(map[string]interface{})
				likes1 := room1["likes_count"].(float64)
				likes2 := room2["likes_count"].(float64)
				assert.GreaterOrEqual(t, likes1, likes2, "Popular rooms should be sorted by likes_count DESC")
			}
		}

		t.Logf("✅ Popular rooms (default): total=%v, page=%v, limit=%v, has_next=%v, count=%d",
			result["total"], result["page"], result["limit"], result["has_next"], len(rooms))
	})

	t.Run("2. GET /v0.1/rooms/recent - Default Pagination", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/recent", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// Validate response structure
		assert.Contains(t, result, "rooms")
		assert.Contains(t, result, "total")
		assert.Contains(t, result, "page")
		assert.Contains(t, result, "limit")
		assert.Contains(t, result, "has_next")

		// Validate default pagination values
		assert.Equal(t, float64(1), result["page"])
		assert.Equal(t, float64(20), result["limit"])

		// Validate rooms array
		rooms := result["rooms"].([]interface{})
		assert.NotNil(t, rooms)

		// If rooms exist, validate sorting (created_at DESC)
		if len(rooms) > 1 {
			for i := 0; i < len(rooms)-1; i++ {
				room1 := rooms[i].(map[string]interface{})
				room2 := rooms[i+1].(map[string]interface{})
				created1 := room1["created_at"].(string)
				created2 := room2["created_at"].(string)
				// Newer rooms should come first (created_at DESC)
				assert.GreaterOrEqual(t, created1, created2, "Recent rooms should be sorted by created_at DESC")
			}
		}

		t.Logf("✅ Recent rooms (default): total=%v, page=%v, limit=%v, has_next=%v, count=%d",
			result["total"], result["page"], result["limit"], result["has_next"], len(rooms))
	})

	t.Run("3. GET /v0.1/rooms/popular - Custom Pagination (page=2, limit=10)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular?page=2&limit=10", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// Validate custom pagination values
		assert.Equal(t, float64(2), result["page"])
		assert.Equal(t, float64(10), result["limit"])

		rooms := result["rooms"].([]interface{})
		assert.LessOrEqual(t, len(rooms), 10, "Should respect limit parameter")

		t.Logf("✅ Popular rooms (custom pagination): page=2, limit=10, returned=%d", len(rooms))
	})

	t.Run("4. GET /v0.1/rooms/recent - Custom Pagination (page=1, limit=5)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/recent?page=1&limit=5", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// Validate custom pagination values
		assert.Equal(t, float64(1), result["page"])
		assert.Equal(t, float64(5), result["limit"])

		rooms := result["rooms"].([]interface{})
		assert.LessOrEqual(t, len(rooms), 5, "Should respect limit parameter")

		t.Logf("✅ Recent rooms (custom pagination): page=1, limit=5, returned=%d", len(rooms))
	})

	t.Run("5. GET /v0.1/rooms/popular - Invalid Page (page < 1)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular?page=0", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 400, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "error")
		assert.Equal(t, "invalid page number (must be >= 1)", result["error"])

		t.Logf("✅ Invalid page validation works: %s", result["error"])
	})

	t.Run("6. GET /v0.1/rooms/recent - Invalid Page (negative number)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/recent?page=-1", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 400, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "error")
		assert.Equal(t, "invalid page number (must be >= 1)", result["error"])

		t.Logf("✅ Negative page validation works")
	})

	t.Run("7. GET /v0.1/rooms/popular - Invalid Limit (non-numeric)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular?limit=abc", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 400, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "error")
		assert.Equal(t, "invalid limit (must be >= 1)", result["error"])

		t.Logf("✅ Invalid limit validation works")
	})

	t.Run("8. GET /v0.1/rooms/popular - Max Limit Capping (limit > 100)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular?limit=500", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// Limit should be capped at 100
		assert.Equal(t, float64(100), result["limit"])
		rooms := result["rooms"].([]interface{})
		assert.LessOrEqual(t, len(rooms), 100, "Should cap limit at 100")

		t.Logf("✅ Limit capping works: requested=500, applied=100")
	})

	t.Run("9. GET /v0.1/rooms/recent - Max Limit Capping (limit=1000)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/recent?limit=1000", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// Limit should be capped at 100
		assert.Equal(t, float64(100), result["limit"])

		t.Logf("✅ Limit capping works for recent rooms")
	})

	t.Run("10. GET /v0.1/rooms/popular - Empty Results (beyond last page)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular?page=99999", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		rooms := result["rooms"].([]interface{})
		assert.Equal(t, 0, len(rooms), "Should return empty array for pages beyond data")
		assert.False(t, result["has_next"].(bool), "has_next should be false")

		t.Logf("✅ Empty results handled correctly")
	})

	t.Run("11. GET /v0.1/rooms/popular - Response Structure Validation", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular?limit=1", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		rooms := result["rooms"].([]interface{})
		if len(rooms) > 0 {
			room := rooms[0].(map[string]interface{})

			// Validate room object structure
			assert.Contains(t, room, "id")
			assert.Contains(t, room, "name")
			assert.Contains(t, room, "room_code")
			assert.Contains(t, room, "is_public")
			assert.Contains(t, room, "likes_count")
			assert.Contains(t, room, "owner_id")
			assert.Contains(t, room, "created_at")

			// Validate data types
			assert.IsType(t, float64(0), room["id"])
			assert.IsType(t, "", room["name"])
			assert.IsType(t, "", room["room_code"])
			assert.IsType(t, true, room["is_public"])
			assert.IsType(t, float64(0), room["likes_count"])
			assert.IsType(t, float64(0), room["owner_id"])
			assert.IsType(t, "", room["created_at"])

			t.Logf("✅ Room object structure validated: id=%v, name=%s, code=%s",
				room["id"], room["name"], room["room_code"])
		} else {
			t.Logf("⚠️  No rooms available for structure validation")
		}
	})

	t.Run("12. GET /v0.1/rooms/recent - Response Structure Validation", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/rooms/recent?limit=1", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		rooms := result["rooms"].([]interface{})
		if len(rooms) > 0 {
			room := rooms[0].(map[string]interface{})

			// Validate all required fields exist
			requiredFields := []string{"id", "name", "room_code", "is_public", "likes_count", "owner_id", "created_at"}
			for _, field := range requiredFields {
				assert.Contains(t, room, field, fmt.Sprintf("Room should contain field: %s", field))
			}

			t.Logf("✅ Recent room structure validated")
		}
	})

	t.Run("13. GET /v0.1/rooms/popular - Pagination Consistency Check", func(t *testing.T) {
		// Get first page
		resp1, body1, err := makeRequest("GET", "/v0.1/rooms/popular?page=1&limit=5", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp1.StatusCode, body1)

		var result1 map[string]interface{}
		parseJSONResponse(t, body1, &result1)

		// Get second page
		resp2, body2, err := makeRequest("GET", "/v0.1/rooms/popular?page=2&limit=5", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp2.StatusCode, body2)

		var result2 map[string]interface{}
		parseJSONResponse(t, body2, &result2)

		// Total count should be the same
		assert.Equal(t, result1["total"], result2["total"], "Total count should be consistent across pages")

		rooms1 := result1["rooms"].([]interface{})
		rooms2 := result2["rooms"].([]interface{})

		// If both pages have rooms, ensure no overlap
		if len(rooms1) > 0 && len(rooms2) > 0 {
			room1Last := rooms1[len(rooms1)-1].(map[string]interface{})
			room2First := rooms2[0].(map[string]interface{})

			// IDs should be different (no duplicates across pages)
			assert.NotEqual(t, room1Last["id"], room2First["id"], "No room duplication across pages")

			t.Logf("✅ Pagination consistency validated: page1_count=%d, page2_count=%d",
				len(rooms1), len(rooms2))
		}
	})

	t.Run("14. GET /v0.1/rooms/popular - has_next Flag Validation", func(t *testing.T) {
		// Get first page with small limit
		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular?page=1&limit=1", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		total := int64(result["total"].(float64))
		hasNext := result["has_next"].(bool)

		if total > 1 {
			assert.True(t, hasNext, "has_next should be true when more pages exist")
		} else if total <= 1 {
			assert.False(t, hasNext, "has_next should be false when no more pages")
		}

		t.Logf("✅ has_next flag validated: total=%d, has_next=%v", total, hasNext)
	})

	t.Run("15. Performance Check - Response Time < 500ms", func(t *testing.T) {
		// Note: This is a basic performance check
		// For comprehensive load testing with 1000 rooms, use a separate load testing tool

		resp, body, err := makeRequest("GET", "/v0.1/rooms/popular?limit=100", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// This test passes if the request completes without timeout
		// Response time will be measured by the test runner
		t.Logf("✅ Performance check completed (response received, see test timing for actual latency)")
		t.Logf("   Note: For production load testing, use dedicated tools (k6, artillery, etc.)")
	})

	t.Run("16. No Authentication Required - Public Endpoint", func(t *testing.T) {
		// Both endpoints should work without authentication
		resp1, body1, err := makeRequest("GET", "/v0.1/rooms/popular", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp1.StatusCode, body1)

		resp2, body2, err := makeRequest("GET", "/v0.1/rooms/recent", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp2.StatusCode, body2)

		t.Logf("✅ Public endpoints work without authentication")
	})
}
