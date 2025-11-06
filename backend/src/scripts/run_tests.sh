#!/bin/bash

# Play Daily Backend E2E Tests Runner
# 백엔드 API E2E 테스트를 실행하는 스크립트

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Play Daily Backend E2E Tests${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""

# 현재 디렉토리 확인
if [ ! -f "main.go" ]; then
    echo -e "${RED}Error: main.go not found. Please run this script from backend/src directory${NC}"
    exit 1
fi

# 테스트 전용 환경 변수 설정
echo -e "${YELLOW}Setting up test environment...${NC}"
export DB_USER=${DB_USER:-root}
export DB_PASSWORD=${DB_PASSWORD:-examplepassword}
export DB_HOST=${DB_HOST:-13.203.37.93}
export DB_PORT=${DB_PORT:-3306}
export DB_NAME=daily_test  # 테스트 전용 DB
export GO_ENV=local

echo -e "${GREEN}✓ Test database: ${DB_HOST}:${DB_PORT}/${DB_NAME}${NC}"

# Go 모듈 다운로드
echo -e "${YELLOW}Downloading dependencies...${NC}"
go mod download

# 테스트 실행
echo -e "${YELLOW}Running E2E tests...${NC}"
echo ""

# 테스트 모드 선택
if [ "$1" == "all" ] || [ -z "$1" ]; then
    echo -e "${YELLOW}Running all tests...${NC}"
    go test -v ./tests/e2e/... -timeout 120s
elif [ "$1" == "auth" ]; then
    echo -e "${YELLOW}Running auth tests...${NC}"
    go test -v ./tests/e2e -run TestAuthFlow -timeout 30s
elif [ "$1" == "memo" ]; then
    echo -e "${YELLOW}Running memo tests...${NC}"
    go test -v ./tests/e2e -run TestMemoFlow -timeout 60s
elif [ "$1" == "category" ]; then
    echo -e "${YELLOW}Running category tests...${NC}"
    go test -v ./tests/e2e -run TestCategoryFlow -timeout 30s
elif [ "$1" == "comment" ]; then
    echo -e "${YELLOW}Running comment tests...${NC}"
    go test -v ./tests/e2e -run TestCommentFlow -timeout 60s
elif [ "$1" == "room" ]; then
    echo -e "${YELLOW}Running room tests...${NC}"
    go test -v ./tests/e2e -run TestRoomFlow -timeout 30s
elif [ "$1" == "coverage" ]; then
    echo -e "${YELLOW}Running tests with coverage...${NC}"
    go test -v -coverprofile=coverage.out ./tests/e2e/... -timeout 120s
    go tool cover -html=coverage.out -o coverage.html
    echo -e "${GREEN}✓ Coverage report generated: coverage.html${NC}"
else
    echo -e "${RED}Unknown test mode: $1${NC}"
    echo "Usage: $0 [all|auth|memo|category|comment|room|coverage]"
    exit 1
fi

# 테스트 결과
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✓ All tests passed!${NC}"
    echo -e "${GREEN}========================================${NC}"
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}✗ Tests failed${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi
