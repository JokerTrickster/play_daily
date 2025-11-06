# Play Daily Backend E2E Tests

## 개요

이 디렉토리는 Play Daily 백엔드 API의 E2E (End-to-End) 테스트를 포함합니다.
모든 주요 API 엔드포인트를 테스트하여 배포 전 API의 정상 동작을 보장합니다.

## 테스트 구조

```
tests/e2e/
├── setup_test.go       # 테스트 환경 설정 및 헬퍼 함수
├── auth_test.go        # 인증 API 테스트 (회원가입, 로그인)
├── memo_test.go        # 메모 CRUD API 테스트
├── category_test.go    # 카테고리 API 테스트
├── comment_test.go     # 댓글 API 테스트
├── room_test.go        # Room API 테스트
└── README.md          # 이 파일
```

## 테스트 실행 방법

### 1. 필수 요구사항

- Go 1.21 이상
- MySQL 데이터베이스 (테스트 환경)
- `.env` 파일 설정

### 2. 의존성 설치

```bash
cd /Users/luxrobo/project/play_daily/backend/src
go mod download
```

### 3. 테스트 실행

**전체 E2E 테스트 실행:**
```bash
cd /Users/luxrobo/project/play_daily/backend/src
go test -v ./tests/e2e/...
```

**특정 테스트만 실행:**
```bash
# 인증 테스트만 실행
go test -v ./tests/e2e -run TestAuthFlow

# 메모 테스트만 실행
go test -v ./tests/e2e -run TestMemoFlow

# 카테고리 테스트만 실행
go test -v ./tests/e2e -run TestCategoryFlow

# 댓글 테스트만 실행
go test -v ./tests/e2e -run TestCommentFlow

# Room 테스트만 실행
go test -v ./tests/e2e -run TestRoomFlow
```

**테스트 커버리지 확인:**
```bash
go test -v -coverprofile=coverage.out ./tests/e2e/...
go tool cover -html=coverage.out -o coverage.html
```

## 테스트 시나리오

### 1. 인증 API (auth_test.go)
- ✅ 회원가입 성공
- ✅ 중복 회원가입 실패
- ✅ 로그인 성공
- ✅ 잘못된 비밀번호로 로그인 실패
- ✅ 존재하지 않는 계정으로 로그인 실패
- ✅ 토큰으로 프로필 조회
- ✅ 토큰 없이 프로필 조회 (개발 모드)

### 2. 메모 API (memo_test.go)
- ✅ 메모 생성 (리스트 모드)
- ✅ 메모 생성 (지도 모드 - 위치 정보 포함)
- ✅ 필수 필드 누락으로 생성 실패
- ✅ 카테고리 없이 생성 실패
- ✅ 메모 목록 조회
- ✅ 단일 메모 조회
- ✅ 메모 수정
- ✅ 존재하지 않는 메모 조회 실패
- ✅ 메모 삭제
- ✅ 위시리스트 메모 생성 및 조회
- ✅ 카테고리 필터로 조회

### 3. 카테고리 API (category_test.go)
- ✅ 카테고리 목록 조회
- ✅ 카테고리 감정별 필터링
- ✅ 카테고리 정렬 순서 검증
- ✅ 토큰 없이 조회 (public API)

### 4. 댓글 API (comment_test.go)
- ✅ 댓글 생성 성공
- ✅ 빈 내용으로 생성 실패
- ✅ 존재하지 않는 메모에 댓글 생성 실패
- ✅ 댓글 목록 조회
- ✅ 댓글 수정
- ✅ 다른 사용자의 댓글 수정 시도 (권한 없음)
- ✅ 댓글 삭제
- ✅ 여러 댓글 생성 및 순서 확인

### 5. Room API (room_test.go)
- ✅ Room 정보 조회
- ✅ Room 수정
- ✅ 존재하지 않는 Room 조회 실패
- ✅ Room 코드로 조회
- ✅ Room 공유
- ✅ Room 좋아요 추가
- ✅ Room 좋아요 취소
- ✅ 본인 Room에 좋아요 시도 (실패)
- ✅ 잘못된 Room 코드로 조회 실패

## CI/CD 통합

### GitHub Actions에서 사용 (향후 적용)

```yaml
name: Backend E2E Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: testpassword
          MYSQL_DATABASE: daily_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - uses: actions/checkout@v3

      - name: Set up Go
        uses: actions/setup-go@v4
        with:
          go-version: '1.21'

      - name: Install dependencies
        run: |
          cd backend/src
          go mod download

      - name: Run E2E tests
        run: |
          cd backend/src
          go test -v ./tests/e2e/...
        env:
          DB_HOST: 127.0.0.1
          DB_PORT: 3306
          DB_USER: root
          DB_PASSWORD: testpassword
          DB_NAME: daily_test
```

## 주의사항

1. **테스트 데이터베이스**: 실제 운영 DB가 아닌 테스트 전용 DB를 사용하세요.
2. **순서 의존성**: `TestAuthFlow`가 먼저 실행되어야 다른 테스트에서 사용할 토큰이 생성됩니다.
3. **데이터 정리**: 각 테스트는 생성한 데이터를 삭제하지만, 테스트 실패 시 남을 수 있습니다.
4. **병렬 실행**: 현재는 순차 실행을 권장합니다 (테스트 간 의존성 때문).

## 트러블슈팅

### 테스트 실패 시

1. **환경 변수 확인**: `.env` 파일이 올바르게 설정되었는지 확인
2. **데이터베이스 연결**: MySQL이 실행 중이고 접근 가능한지 확인
3. **포트 충돌**: 7001 포트가 이미 사용 중이지 않은지 확인
4. **로그 확인**: `-v` 플래그로 상세 로그 확인

### 카테고리 테스트 실패 시

카테고리 데이터가 DB에 없으면 테스트가 실패할 수 있습니다.
다음 SQL을 실행하여 초기 카테고리 데이터를 삽입하세요:

```sql
-- features/migration/V1__initial_categories.sql 참조
```

## 기여 가이드

새로운 API를 추가할 때는 해당 E2E 테스트도 함께 작성하세요.

1. `tests/e2e/` 디렉토리에 새 파일 생성 (예: `new_feature_test.go`)
2. 테스트 함수명은 `TestXXXFlow` 형식으로 작성
3. 성공 케이스와 실패 케이스 모두 포함
4. README에 테스트 시나리오 추가
