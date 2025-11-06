package e2e

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

var (
	testMemoID      uint
	testCategoryIDs = []int{1, 2} // 테스트용 카테고리 ID (실제 DB에 존재해야 함)
)

// TestMemoFlow는 메모 CRUD 플로우 전체를 테스트합니다
func TestMemoFlow(t *testing.T) {
	// 인증 토큰이 필요하므로 먼저 인증 테스트 실행
	if testToken == "" {
		t.Skip("인증 토큰이 없습니다. TestAuthFlow를 먼저 실행하세요.")
	}

	t.Run("1. 메모 생성 성공 (리스트 모드)", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"room_id":       testRoomID,
			"title":         "테스트 메모",
			"creation_mode": "list",
			"category_ids":  testCategoryIDs,
			"rating":        4,
			"is_pinned":     false,
			"is_wishlist":   false,
		}

		resp, body, err := makeRequest("POST", "/v0.1/memo", reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 201, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Contains(t, result, "id")
		assert.Contains(t, result, "title")
		assert.Equal(t, "테스트 메모", result["title"])

		testMemoID = uint(result["id"].(float64))
		t.Logf("메모 생성 성공 - MemoID: %d", testMemoID)
	})

	t.Run("2. 메모 생성 성공 (지도 모드 - 위치 정보 포함)", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"room_id":          testRoomID,
			"title":            "강남역 카페",
			"creation_mode":    "map",
			"category_ids":     []int{1},
			"rating":           5,
			"is_pinned":        true,
			"is_wishlist":      false,
			"latitude":         37.498095,
			"longitude":        127.027610,
			"location_name":    "강남역",
			"business_name":    "스타벅스 강남역점",
			"business_address": "서울 강남구 강남대로 지하 396",
			"naver_place_url":  "https://naver.me/example",
		}

		resp, body, err := makeRequest("POST", "/v0.1/memo", reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 201, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Equal(t, "강남역 카페", result["title"])
		assert.Equal(t, "map", result["creation_mode"])
		assert.NotNil(t, result["latitude"])
		assert.NotNil(t, result["longitude"])
	})

	t.Run("3. 필수 필드 누락으로 메모 생성 실패", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"room_id": testRoomID,
			// title 누락
			"creation_mode": "list",
			"category_ids":  testCategoryIDs,
		}

		resp, body, err := makeRequest("POST", "/v0.1/memo", reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 400, resp.StatusCode, body)
	})

	t.Run("4. 카테고리 없이 메모 생성 실패", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"room_id":       testRoomID,
			"title":         "카테고리 없는 메모",
			"creation_mode": "list",
			"category_ids":  []int{}, // 빈 배열
		}

		resp, body, err := makeRequest("POST", "/v0.1/memo", reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 400, resp.StatusCode, body)
	})

	t.Run("5. 메모 목록 조회 성공", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/memo?room_id=%d&page=1&limit=10", testRoomID)
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Contains(t, result, "memos")
		assert.Contains(t, result, "total")
		assert.Contains(t, result, "page")
		assert.Contains(t, result, "limit")

		memos := result["memos"].([]interface{})
		assert.Greater(t, len(memos), 0, "최소 1개 이상의 메모가 있어야 합니다")
	})

	t.Run("6. 단일 메모 조회 성공", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/memo/%d", testMemoID)
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Equal(t, float64(testMemoID), result["id"])
		assert.Equal(t, "테스트 메모", result["title"])
		assert.Contains(t, result, "categories")
	})

	t.Run("7. 메모 수정 성공", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"title":     "수정된 테스트 메모",
			"rating":    5,
			"is_pinned": true,
		}

		url := fmt.Sprintf("/v0.1/memo/%d", testMemoID)
		resp, body, err := makeRequest("PUT", url, reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Equal(t, "수정된 테스트 메모", result["title"])
		assert.Equal(t, float64(5), result["rating"])
		assert.Equal(t, true, result["is_pinned"])
	})

	t.Run("8. 존재하지 않는 메모 조회 실패", func(t *testing.T) {
		url := "/v0.1/memo/999999"
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 404, resp.StatusCode, body)
	})

	t.Run("9. 메모 삭제 성공", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/memo/%d", testMemoID)
		resp, body, err := makeRequest("DELETE", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		// 삭제 후 조회 시 404 반환되는지 확인
		resp, body, err = makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 404, resp.StatusCode, body)
	})

	t.Run("10. 위시리스트 메모 생성 및 조회", func(t *testing.T) {
		// 위시리스트 메모 생성
		reqBody := map[string]interface{}{
			"room_id":       testRoomID,
			"title":         "가고 싶은 곳",
			"creation_mode": "list",
			"category_ids":  testCategoryIDs,
			"rating":        3, // 관심도
			"is_wishlist":   true,
		}

		resp, body, err := makeRequest("POST", "/v0.1/memo", reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 201, resp.StatusCode, body)

		var createResult map[string]interface{}
		parseJSONResponse(t, body, &createResult)
		wishlistMemoID := uint(createResult["id"].(float64))

		// 위시리스트 필터로 조회
		url := fmt.Sprintf("/v0.1/memo?room_id=%d&is_wishlist=true", testRoomID)
		resp, body, err = makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var listResult map[string]interface{}
		parseJSONResponse(t, body, &listResult)

		memos := listResult["memos"].([]interface{})
		found := false
		for _, memo := range memos {
			m := memo.(map[string]interface{})
			if uint(m["id"].(float64)) == wishlistMemoID {
				found = true
				assert.Equal(t, true, m["is_wishlist"])
				break
			}
		}
		assert.True(t, found, "위시리스트 메모가 목록에서 발견되어야 합니다")
	})

	t.Run("11. 카테고리 필터로 메모 조회", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/memo?room_id=%d&category_ids=%d", testRoomID, testCategoryIDs[0])
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Contains(t, result, "memos")
		// 카테고리 필터링이 제대로 동작하는지 확인
	})
}
