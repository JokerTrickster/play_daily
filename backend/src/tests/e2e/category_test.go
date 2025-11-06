package e2e

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

// TestCategoryFlow는 카테고리 API 플로우를 테스트합니다
func TestCategoryFlow(t *testing.T) {
	t.Run("1. 카테고리 목록 조회 성공", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/categories", nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var wrapper map[string]interface{}
		parseJSONResponse(t, body, &wrapper)

		categories := wrapper["categories"].([]interface{})
		assert.Greater(t, len(categories), 0, "최소 1개 이상의 카테고리가 있어야 합니다")

		// 첫 번째 카테고리의 구조 검증
		if len(categories) > 0 {
			category := categories[0].(map[string]interface{})
			assert.Contains(t, category, "id")
			assert.Contains(t, category, "name")
			assert.Contains(t, category, "sentiment")
			assert.Contains(t, category, "color")
			assert.Contains(t, category, "display_order")

			// sentiment 값이 유효한지 확인
			sentiment := category["sentiment"].(string)
			assert.Contains(t, []string{"positive", "negative", "neutral"}, sentiment)
		}
	})

	t.Run("2. 카테고리 감정별 필터링", func(t *testing.T) {
		// 먼저 전체 카테고리 조회
		resp, body, err := makeRequest("GET", "/v0.1/categories", nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var wrapper map[string]interface{}
		parseJSONResponse(t, body, &wrapper)
		allCategories := wrapper["categories"].([]interface{})

		// 감정별로 분류
		sentimentCounts := map[string]int{
			"positive": 0,
			"negative": 0,
			"neutral":  0,
		}

		for _, cat := range allCategories {
			category := cat.(map[string]interface{})
			sentiment := category["sentiment"].(string)
			sentimentCounts[sentiment]++
		}

		t.Logf("카테고리 감정별 분포 - Positive: %d, Negative: %d, Neutral: %d",
			sentimentCounts["positive"], sentimentCounts["negative"], sentimentCounts["neutral"])

		// 각 감정 타입별로 최소 1개씩은 있어야 함
		assert.Greater(t, sentimentCounts["positive"], 0, "긍정 카테고리가 최소 1개는 있어야 합니다")
		assert.Greater(t, sentimentCounts["negative"], 0, "부정 카테고리가 최소 1개는 있어야 합니다")
		assert.Greater(t, sentimentCounts["neutral"], 0, "중립 카테고리가 최소 1개는 있어야 합니다")
	})

	t.Run("3. 카테고리 정렬 순서 검증", func(t *testing.T) {
		resp, body, err := makeRequest("GET", "/v0.1/categories", nil, testToken)
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var wrapper map[string]interface{}
		parseJSONResponse(t, body, &wrapper)
		categories := wrapper["categories"].([]interface{})

		// display_order가 올바르게 정렬되어 있는지 확인
		for i := 1; i < len(categories); i++ {
			prevOrder := categories[i-1].(map[string]interface{})["display_order"].(float64)
			currOrder := categories[i].(map[string]interface{})["display_order"].(float64)
			assert.LessOrEqual(t, prevOrder, currOrder, "카테고리가 display_order 순으로 정렬되어야 합니다")
		}
	})

	t.Run("4. 토큰 없이 카테고리 조회 (public API)", func(t *testing.T) {
		// 카테고리는 public API이므로 토큰 없이도 조회 가능
		resp, body, err := makeRequest("GET", "/v0.1/categories", nil, "")
		assert.NoError(t, err)
		assertStatusCode(t, 200, resp.StatusCode, body)

		var wrapper map[string]interface{}
		parseJSONResponse(t, body, &wrapper)
		categories := wrapper["categories"].([]interface{})
		assert.Greater(t, len(categories), 0)
	})
}
