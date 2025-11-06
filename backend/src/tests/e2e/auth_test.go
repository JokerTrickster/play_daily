package e2e

import (
	"fmt"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

// TestAuthFlow는 인증 플로우 전체를 테스트합니다
func TestAuthFlow(t *testing.T) {
	// 고유한 계정 ID 생성 (타임스탬프 기반)
	timestamp := time.Now().UnixNano()
	accountID := fmt.Sprintf("testuser_%d", timestamp)
	password := "testpass123"

	t.Run("1. 회원가입 성공", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"account_id": accountID,
			"password":   password,
		}

		resp, body, err := makeRequest("POST", "/v0.1/auth/signup", reqBody, "")
		assert.NoError(t, err)
		assertStatusCode(t, 201, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// 응답에 토큰이 포함되어야 함
		assert.Contains(t, result, "access_token")
		assert.Contains(t, result, "user_id")
		assert.Contains(t, result, "default_room_id")

		// 테스트에 사용할 토큰 저장
		testToken = result["access_token"].(string)
		testUserID = uint(result["user_id"].(float64))
		testRoomID = uint(result["default_room_id"].(float64))
		testEmail = accountID

		t.Logf("회원가입 성공 - UserID: %d, RoomID: %d", testUserID, testRoomID)
	})

	t.Run("2. 중복 회원가입 실패", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"account_id": accountID,
			"password":   password,
		}

		resp, body, err := makeRequest("POST", "/v0.1/auth/signup", reqBody, "")
		assert.NoError(t, err)
		assertStatusCode(t, 400, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "error")
	})

	t.Run("3. 로그인 성공", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"account_id": accountID,
			"password":   password,
		}

		resp, body, err := makeRequest("POST", "/v0.1/auth/signin", reqBody, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Contains(t, result, "access_token")
		assert.Contains(t, result, "user_id")
		assert.NotEmpty(t, result["access_token"])
	})

	t.Run("4. 잘못된 비밀번호로 로그인 실패", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"account_id": accountID,
			"password":   "wrongpassword",
		}

		resp, body, err := makeRequest("POST", "/v0.1/auth/signin", reqBody, "")
		assert.NoError(t, err)
		assertStatusCode(t, 401, resp.StatusCode, body)
	})

	t.Run("5. 존재하지 않는 계정으로 로그인 실패", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"account_id": "nonexistent_user",
			"password":   password,
		}

		resp, body, err := makeRequest("POST", "/v0.1/auth/signin", reqBody, "")
		assert.NoError(t, err)
		assertStatusCode(t, 404, resp.StatusCode, body)
	})

	t.Run("6. 토큰으로 프로필 조회 성공", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/profile", nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Contains(t, result, "user_id")
		assert.Contains(t, result, "account_id")
		assert.Equal(t, accountID, result["account_id"])
	})

	t.Run("7. 토큰 없이 프로필 조회 (개발 모드에서는 성공)", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/profile", nil, "")
		assert.NoError(t, err)
		// 개발 모드에서는 기본 user ID 1로 접근 가능
		assertStatusCode(t, 200, resp.StatusCode, body)
	})
}
