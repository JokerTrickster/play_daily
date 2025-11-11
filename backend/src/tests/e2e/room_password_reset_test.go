package e2e

import (
	"fmt"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

// TestRoomPasswordResetFlow tests the room password reset API flow
func TestRoomPasswordResetFlow(t *testing.T) {
	if testToken == "" {
		t.Skip("인증 토큰이 없습니다. TestAuthFlow를 먼저 실행하세요.")
	}

	var (
		newPassword1 string
		newPassword2 string
		newPassword3 string
	)

	t.Run("1. Room 비밀번호 리셋 성공 - 첫 번째", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/rooms/%d/reset-password", testRoomID)
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		// Validate response structure
		assert.Contains(t, result, "success")
		assert.Contains(t, result, "new_password")
		assert.Contains(t, result, "message")
		assert.True(t, result["success"].(bool))

		// Validate password format (12 characters)
		newPassword1 = result["new_password"].(string)
		assert.Equal(t, 12, len(newPassword1))

		// Validate password contains required character types
		assert.Regexp(t, `[A-Z]`, newPassword1, "Password should contain uppercase")
		assert.Regexp(t, `[a-z]`, newPassword1, "Password should contain lowercase")
		assert.Regexp(t, `[0-9]`, newPassword1, "Password should contain digit")
		assert.Regexp(t, `[!@#$%^&*]`, newPassword1, "Password should contain special char")

		t.Logf("✅ 첫 번째 비밀번호 리셋 성공: %s", newPassword1)
	})

	t.Run("2. Room 비밀번호 리셋 성공 - 두 번째 (다른 비밀번호 생성)", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/rooms/%d/reset-password", testRoomID)
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		newPassword2 = result["new_password"].(string)
		assert.NotEqual(t, newPassword1, newPassword2, "두 번째 비밀번호는 첫 번째와 달라야 합니다")

		t.Logf("✅ 두 번째 비밀번호 리셋 성공: %s", newPassword2)
	})

	t.Run("3. Room 비밀번호 리셋 성공 - 세 번째", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/rooms/%d/reset-password", testRoomID)
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		newPassword3 = result["new_password"].(string)
		assert.NotEqual(t, newPassword1, newPassword3)
		assert.NotEqual(t, newPassword2, newPassword3)

		t.Logf("✅ 세 번째 비밀번호 리셋 성공: %s", newPassword3)
	})

	t.Run("4. Rate Limiting - 네 번째 리셋 시도 (429 에러 예상)", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/rooms/%d/reset-password", testRoomID)
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 429, resp.StatusCode, body)

		// Verify Retry-After header exists
		retryAfter := resp.Header.Get("Retry-After")
		assert.NotEmpty(t, retryAfter, "Retry-After 헤더가 있어야 합니다")
		assert.Equal(t, "86400", retryAfter, "Retry-After는 86400초(24시간)여야 합니다")

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "error")
		assert.Contains(t, result["error"].(string), "rate limit exceeded")

		t.Logf("✅ Rate Limiting 정상 작동: %s", result["error"])
	})

	t.Run("5. 존재하지 않는 Room 리셋 시도 (404 에러 예상)", func(t *testing.T) {
		url := "/v0.1/rooms/999999/reset-password"
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 404, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "error")
		assert.Equal(t, "room not found", result["error"])

		t.Logf("✅ 존재하지 않는 Room 처리 완료")
	})

	t.Run("6. 권한 없는 사용자의 리셋 시도 (403 에러 예상)", func(t *testing.T) {
		if testSharedRoomID == 0 {
			t.Skip("공유된 Room이 없습니다")
		}

		// testSharedRoomID is owned by another user
		url := fmt.Sprintf("/v0.1/rooms/%d/reset-password", testSharedRoomID)
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 403, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "error")
		assert.Equal(t, "only room owner can reset password", result["error"])

		t.Logf("✅ 권한 검증 정상 작동")
	})

	t.Run("7. 잘못된 Room ID 형식 (400 에러 예상)", func(t *testing.T) {
		url := "/v0.1/rooms/invalid-id/reset-password"
		resp, body, err := makeRequest("POST", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 400, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		assert.Contains(t, result, "error")

		t.Logf("✅ 잘못된 Room ID 처리 완료")
	})

	t.Run("8. 인증되지 않은 요청 (401 에러 예상)", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/rooms/%d/reset-password", testRoomID)
		resp, body, err := makeRequest("POST", url, nil, "") // No token
		assert.NoError(t, err)
		assertStatusCode(t, 401, resp.StatusCode, body)

		t.Logf("✅ 인증 검증 정상 작동")
	})

	t.Run("9. 새로운 비밀번호로 Room 접속 가능 확인", func(t *testing.T) {
		// Create a new user to test joining with the new password
		timestamp := fmt.Sprintf("%d", time.Now().Unix())
		newUserReq := map[string]interface{}{
			"account_id": "testuser_pwreset_" + timestamp,
			"password":   "testpass123",
		}

		resp, body, err := makeRequest("POST", "/v0.1/auth/signup", newUserReq, "")
		assert.NoError(t, err)

		var signupResult map[string]interface{}
		parseJSONResponse(t, body, &signupResult)
		newUserToken := signupResult["access_token"].(string)

		// Try to join the room with the latest reset password (newPassword3)
		joinReq := map[string]interface{}{
			"room_id":       testRoomID,
			"room_password": newPassword3,
		}

		resp, body, err = makeRequest("POST", "/v0.1/room/join", joinReq, newUserToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var joinResult map[string]interface{}
		parseJSONResponse(t, body, &joinResult)
		assert.Equal(t, float64(testRoomID), joinResult["id"])

		t.Logf("✅ 새 비밀번호로 Room 접속 성공")
	})

	t.Run("10. 비밀번호 보안 강도 검증", func(t *testing.T) {
		// Verify all generated passwords meet security requirements
		passwords := []string{newPassword1, newPassword2, newPassword3}

		for i, pwd := range passwords {
			// Length check
			assert.Equal(t, 12, len(pwd), "비밀번호 %d 길이는 12자여야 합니다", i+1)

			// Character type checks
			hasUpper := false
			hasLower := false
			hasDigit := false
			hasSpecial := false

			for _, ch := range pwd {
				if ch >= 'A' && ch <= 'Z' {
					hasUpper = true
				} else if ch >= 'a' && ch <= 'z' {
					hasLower = true
				} else if ch >= '0' && ch <= '9' {
					hasDigit = true
				} else if ch == '!' || ch == '@' || ch == '#' || ch == '$' || ch == '%' || ch == '^' || ch == '&' || ch == '*' {
					hasSpecial = true
				}
			}

			assert.True(t, hasUpper, "비밀번호 %d는 대문자를 포함해야 합니다", i+1)
			assert.True(t, hasLower, "비밀번호 %d는 소문자를 포함해야 합니다", i+1)
			assert.True(t, hasDigit, "비밀번호 %d는 숫자를 포함해야 합니다", i+1)
			assert.True(t, hasSpecial, "비밀번호 %d는 특수문자를 포함해야 합니다", i+1)
		}

		t.Logf("✅ 모든 비밀번호가 보안 요구사항을 충족합니다")
	})

	// Note: Audit log verification would require database access
	// This would be tested in integration tests or manual verification
	t.Run("11. Audit Log 생성 확인 (Manual Verification Required)", func(t *testing.T) {
		t.Log("⚠️  Audit log 검증은 데이터베이스 직접 확인이 필요합니다")
		t.Log("    - room_password_resets 테이블에 3개의 레코드가 있어야 합니다")
		t.Log("    - rate limit violation 레코드가 1개 있어야 합니다")
		t.Log("    - 각 레코드에 IP 주소와 User-Agent가 기록되어야 합니다")
		t.Log("    - previous_password_hash와 new_password_hash가 기록되어야 합니다")
	})
}
