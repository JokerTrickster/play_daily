package e2e

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

var testCommentID uint

// TestCommentFlow는 댓글 API 플로우를 테스트합니다
func TestCommentFlow(t *testing.T) {
	if testToken == "" {
		t.Skip("인증 토큰이 없습니다. TestAuthFlow를 먼저 실행하세요.")
	}

	// 테스트용 메모 먼저 생성
	var testMemoForComment uint
	t.Run("0. 댓글 테스트용 메모 생성", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"room_id":       testRoomID,
			"title":         "댓글 테스트 메모",
			"creation_mode": "list",
			"category_ids":  []int{1},
			"rating":        4,
		}

		resp, body, err := makeRequest("POST", "/v0.1/memo", reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 201, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)
		testMemoForComment = uint(result["id"].(float64))
	})

	t.Run("1. 댓글 생성 성공", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"content": "정말 좋은 곳이네요!",
			"rating":  5,
		}

		url := fmt.Sprintf("/v0.1/memo/%d/comments", testMemoForComment)
		resp, body, err := makeRequest("POST", url, reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 201, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Contains(t, result, "id")
		assert.Contains(t, result, "content")
		assert.Equal(t, "정말 좋은 곳이네요!", result["content"])
		assert.Equal(t, float64(5), result["rating"])

		testCommentID = uint(result["id"].(float64))
		t.Logf("댓글 생성 성공 - CommentID: %d", testCommentID)
	})

	t.Run("2. 빈 내용으로 댓글 생성 실패", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"content": "",
			"rating":  3,
		}

		url := fmt.Sprintf("/v0.1/memo/%d/comments", testMemoForComment)
		resp, body, err := makeRequest("POST", url, reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 400, resp.StatusCode, body)
	})

	t.Run("3. 존재하지 않는 메모에 댓글 생성 실패", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"content": "댓글 내용",
			"rating":  4,
		}

		url := "/v0.1/memo/999999/comments"
		resp, body, err := makeRequest("POST", url, reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 404, resp.StatusCode, body)
	})

	t.Run("4. 메모의 댓글 목록 조회 성공", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/memo/%d/comments", testMemoForComment)
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result []interface{}
		parseJSONResponse(t, body, &result)

		assert.Greater(t, len(result), 0, "최소 1개 이상의 댓글이 있어야 합니다")

		// 첫 번째 댓글 검증
		comment := result[0].(map[string]interface{})
		assert.Contains(t, comment, "id")
		assert.Contains(t, comment, "content")
		assert.Contains(t, comment, "rating")
		assert.Contains(t, comment, "user_name")
		assert.Contains(t, comment, "created_at")
	})

	t.Run("5. 댓글 수정 성공", func(t *testing.T) {
		reqBody := map[string]interface{}{
			"content": "수정된 댓글 내용",
			"rating":  4,
		}

		url := fmt.Sprintf("/v0.1/comment/%d", testCommentID)
		resp, body, err := makeRequest("PUT", url, reqBody, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var result map[string]interface{}
		parseJSONResponse(t, body, &result)

		assert.Equal(t, "수정된 댓글 내용", result["content"])
		assert.Equal(t, float64(4), result["rating"])
	})

	t.Run("6. 다른 사용자의 댓글 수정 시도 (권한 없음)", func(t *testing.T) {
		// 새로운 사용자 생성 및 로그인
		timestamp := fmt.Sprintf("%d", uint(testCommentID)+1000)
		newUserReq := map[string]interface{}{
			"account_id": "testuser_comment_" + timestamp,
			"password":   "testpass123",
			"auth_code":  "5508",
			"nickname":   "댓글테스트유저",
		}

		signupResp, signupBody, err := makeRequest("POST", "/v0.1/auth/signup", newUserReq, "")
		assert.NoError(t, err)
		assert.Equal(t, 201, signupResp.StatusCode)

		var signupResult map[string]interface{}
		parseJSONResponse(t, signupBody, &signupResult)
		newUserToken := signupResult["access_token"].(string)

		// 다른 사용자의 토큰으로 댓글 수정 시도
		reqBody := map[string]interface{}{
			"content": "해킹 시도",
			"rating":  1,
		}

		url := fmt.Sprintf("/v0.1/comment/%d", testCommentID)
		resp, body, err := makeRequest("PUT", url, reqBody, newUserToken)
		assert.NoError(t, err)
		assertStatusCode(t, 403, resp.StatusCode, body)
	})

	t.Run("7. 댓글 삭제 성공", func(t *testing.T) {
		url := fmt.Sprintf("/v0.1/comment/%d", testCommentID)
		resp, body, err := makeRequest("DELETE", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		// 삭제 후 댓글 목록에서 사라졌는지 확인
		listURL := fmt.Sprintf("/v0.1/memo/%d/comments", testMemoForComment)
		resp, body, err = makeRequest("GET", listURL, nil, testToken)
		assert.NoError(t, err)

		var comments []interface{}
		parseJSONResponse(t, body, &comments)

		// 삭제된 댓글이 목록에 없는지 확인
		for _, c := range comments {
			comment := c.(map[string]interface{})
			assert.NotEqual(t, float64(testCommentID), comment["id"])
		}
	})

	t.Run("8. 여러 댓글 생성 및 순서 확인", func(t *testing.T) {
		// 3개의 댓글 생성
		contents := []string{"첫 번째 댓글", "두 번째 댓글", "세 번째 댓글"}
		for _, content := range contents {
			reqBody := map[string]interface{}{
				"content": content,
				"rating":  4,
			}

			url := fmt.Sprintf("/v0.1/memo/%d/comments", testMemoForComment)
			resp, body, err := makeRequest("POST", url, reqBody, testToken)
			assert.NoError(t, err)
			assertStatusCode(t, 201, resp.StatusCode, body)
		}

		// 댓글 목록 조회 및 순서 확인
		url := fmt.Sprintf("/v0.1/memo/%d/comments", testMemoForComment)
		resp, body, err := makeRequest("GET", url, nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var comments []interface{}
		parseJSONResponse(t, body, &comments)

		assert.GreaterOrEqual(t, len(comments), 3, "최소 3개의 댓글이 있어야 합니다")

		// 생성 시간 순으로 정렬되어 있는지 확인 (최신순)
		for i := 1; i < len(comments); i++ {
			prevTime := comments[i-1].(map[string]interface{})["created_at"].(string)
			currTime := comments[i].(map[string]interface{})["created_at"].(string)
			assert.GreaterOrEqual(t, prevTime, currTime, "댓글이 최신순으로 정렬되어야 합니다")
		}
	})
}
