package e2e

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"main/common"
	"main/features"
	_middleware "main/middleware"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"

	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
)

var (
	e            *echo.Echo
	testServer   *httptest.Server
	testToken    string
	testUserID   uint
	testRoomID   uint = 1
	testEmail    string
	baseURL      string
)

// TestMain은 모든 테스트 전후에 실행되는 setup/teardown 함수
func TestMain(m *testing.M) {
	// Setup
	if err := setupTestEnvironment(); err != nil {
		fmt.Printf("테스트 환경 설정 실패: %v\n", err)
		os.Exit(1)
	}

	// Run tests
	code := m.Run()

	// Teardown
	teardownTestEnvironment()

	os.Exit(code)
}

// setupTestEnvironment는 테스트 환경을 초기화합니다
func setupTestEnvironment() error {
	// 환경 변수 로드
	if err := common.LoadConfig(); err != nil {
		return fmt.Errorf("환경 변수 로드 실패: %w", err)
	}

	// Echo 인스턴스 생성
	e = echo.New()

	// 서버 초기화
	if err := common.InitServer(); err != nil {
		return fmt.Errorf("서버 초기화 실패: %w", err)
	}

	// 미들웨어 초기화
	if err := _middleware.InitMiddleware(e); err != nil {
		return fmt.Errorf("미들웨어 초기화 실패: %w", err)
	}

	// 라우트 초기화
	if err := features.InitHandler(e); err != nil {
		return fmt.Errorf("핸들러 초기화 실패: %w", err)
	}

	// 테스트 서버 시작
	testServer = httptest.NewServer(e)
	baseURL = testServer.URL

	return nil
}

// teardownTestEnvironment는 테스트 환경을 정리합니다
func teardownTestEnvironment() {
	if testServer != nil {
		testServer.Close()
	}
}

// makeRequest는 HTTP 요청을 생성하고 응답을 반환하는 헬퍼 함수
func makeRequest(method, path string, body interface{}, token string) (*http.Response, []byte, error) {
	var reqBody io.Reader
	if body != nil {
		jsonData, err := json.Marshal(body)
		if err != nil {
			return nil, nil, err
		}
		reqBody = bytes.NewBuffer(jsonData)
	}

	req, err := http.NewRequest(method, baseURL+path, reqBody)
	if err != nil {
		return nil, nil, err
	}

	req.Header.Set("Content-Type", "application/json")
	if token != "" {
		req.Header.Set("tkn", token)
	}

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return nil, nil, err
	}
	defer resp.Body.Close()

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return resp, nil, err
	}

	return resp, respBody, nil
}

// assertStatusCode는 HTTP 상태 코드를 검증하는 헬퍼 함수
func assertStatusCode(t *testing.T, expected int, actual int, body []byte) {
	if expected != actual {
		t.Errorf("Expected status code %d, got %d. Response body: %s", expected, actual, string(body))
	}
	assert.Equal(t, expected, actual)
}

// parseJSONResponse는 JSON 응답을 파싱하는 헬퍼 함수
func parseJSONResponse(t *testing.T, body []byte, target interface{}) {
	err := json.Unmarshal(body, target)
	assert.NoError(t, err, "JSON 파싱 실패: %s", string(body))
}
