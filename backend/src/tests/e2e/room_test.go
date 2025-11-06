package e2e

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

var testSharedRoomID uint

// TestRoomFlow는 Room API 플로우를 테스트합니다
func TestRoomFlow(t *testing.T) {
	if testToken == "" {
		t.Skip("인증 토큰이 없습니다. TestAuthFlow를 먼저 실행하세요.")
	}

	t.Run("1. 기본 Room 정보 조회 성공", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/room/%d", testRoomID)
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Contains(t, result, "id")
		assert.Contains(t, result, "room_code")
		assert.Contains(t, result, "name")
		assert.Equal(t, float64(testRoomID), result["id"])
	})

	t.Run("2. Room 수정 성공", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"name": "수정된 방 이름",
		}

		url := fmt.Sprintf("/v0.1/room/%d", testRoomID)
		resp, body, err := makeRequest("PUT", url, reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Equal(t, "수정된 방 이름", result["name"])
	})

	t.Run("3. 존재하지 않는 Room 조회 실패", func(t *testing.T) {
		url := "/v0.1/room/999999"
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 404, resp.StatusCode, body)
	})

	t.Run("4. Room 코드로 Room 조회 성공", func(t *testing.T) {
		// 먼저 Room 정보를 가져와서 room_code를 얻음
		roomURL := fmt.Sprintf("/v0.1/room/%d", testRoomID)
		resp, body, err := makeRequest("GET", roomURL, nil, testToken)
		assert.NoError(t, err)

		var roomInfo map[string]interface{}
		parseJSONResponse(t, body, &roomInfo)
		roomCode := roomInfo["room_code"].(string)

		// Room 코드로 조회
		codeURL := fmt.Sprintf("/v0.1/room/code/%s", roomCode)
		resp, body, err = makeRequest("GET", codeURL, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Equal(t, roomCode, result["room_code"])
		assert.Equal(t, float64(testRoomID), result["id"])
	})

	t.Run("5. 다른 사용자와 Room 공유", func(t *testing.T) {
		// 새로운 사용자 생성
		timestamp := fmt.Sprintf("%d", testRoomID+2000)
		newUserReq := map[string]interface{}{
			"account_id": "testuser_room_" + timestamp,
			"password":   "testpass123",
		}

		resp, body, err := makeRequest("POST", "/v0.1/auth/signup", newUserReq, "")
		assert.NoError(t, err)

		var signupResult map[string]interface{}
		parseJSONResponse(t, body, &signupResult)
		newUserToken := signupResult["access_token"].(string)
		newUserDefaultRoomID := uint(signupResult["default_room_id"].(float64))

		testSharedRoomID = newUserDefaultRoomID

		// 새 사용자의 Room 코드 가져오기
		roomURL := fmt.Sprintf("/v0.1/room/%d", newUserDefaultRoomID)
		resp, body, err = makeRequest("GET", roomURL, nil, newUserToken)
		assert.NoError(t, err)

		var roomInfo map[string]interface{}
		parseJSONResponse(t, body, &roomInfo)
		sharedRoomCode := roomInfo["room_code"].(string)

		// 첫 번째 사용자가 두 번째 사용자의 Room 코드로 조회
		codeURL := fmt.Sprintf("/v0.1/room/code/%s", sharedRoomCode)
		resp, body, err = makeRequest("GET", codeURL, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var sharedRoom map[string]interface{}
		parseJSONResponse(t, body, &sharedRoom)
		assert.Equal(t, sharedRoomCode, sharedRoom["room_code"])

		t.Logf("Room 공유 성공 - SharedRoomID: %d, RoomCode: %s", newUserDefaultRoomID, sharedRoomCode)
	})

	t.Run("6. Room 좋아요 추가 성공", func(t *testing.T) {
		if testSharedRoomID == 0 {
			t.Skip("공유된 Room이 없습니다")
		}

		url := fmt.Sprintf("/v0.1/room/%d/like", testSharedRoomID)
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "likes_count")
		assert.Greater(t, result["likes_count"].(float64), float64(0))
	})

	t.Run("7. Room 좋아요 취소 성공", func(t *testing.T) {
		if testSharedRoomID == 0 {
			t.Skip("공유된 Room이 없습니다")
		}

		url := fmt.Sprintf("/v0.1/room/%d/like", testSharedRoomID)
		resp, body, err := makeRequest("DELETE", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "likes_count")
	})

	t.Run("8. 본인의 Room에 좋아요 시도 (실패해야 함)", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/room/%d/like", testRoomID)
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		// 본인 Room에는 좋아요를 할 수 없어야 함
		assertStatusCode(t, 400, resp.StatusCode, body)
	})

	t.Run("9. 잘못된 Room 코드로 조회 실패", func(t *testing.T) {
		url := "/v0.1/room/code/invalid-room-code-12345"
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 404, resp.StatusCode, body)
	})
}
