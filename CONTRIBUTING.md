# Contributing to Play Daily

## 개발 워크플로우

### 1. 기능 개발 필수 규칙

**⚠️ IMPORTANT: 모든 새로운 기능 개발 시 E2E 테스트 코드 필수 작성**

```
새 기능 추가 = 코드 구현 + E2E 테스트 + 문서 업데이트
```

#### 테스트 코드 작성 기준

- ✅ **API 엔드포인트 추가/수정** → E2E 테스트 필수
- ✅ **데이터베이스 스키마 변경** → 마이그레이션 + 테스트 업데이트
- ✅ **비즈니스 로직 추가** → 해당 시나리오 테스트 작성
- ✅ **에러 케이스** → 실패 시나리오 테스트 포함

#### 테스트 작성 위치

```
backend/src/tests/e2e/
├── auth_test.go        # 인증 관련
├── memo_test.go        # 메모 관련
├── category_test.go    # 카테고리 관련
├── comment_test.go     # 댓글 관련
├── room_test.go        # Room 관련
└── {feature}_test.go   # 새 기능 추가 시
```

### 2. 개발 프로세스

#### Step 1: 기능 개발
```bash
# Feature branch 생성
git checkout -b feature/your-feature-name

# 코드 구현
# ... develop your feature ...
```

#### Step 2: E2E 테스트 작성 (필수)
```bash
cd backend/src/tests/e2e

# 새 테스트 파일 생성 또는 기존 파일 수정
# 예: vim new_feature_test.go
```

**테스트 작성 예시:**
```go
package e2e

import (
    "testing"
    "github.com/stretchr/testify/assert"
)

func TestNewFeatureFlow(t *testing.T) {
    t.Run("1. 성공 케이스", func(t *testing.T) {
        // Given: 테스트 데이터 준비
        reqBody := map[string]interface{}{
            "field": "value",
        }

        // When: API 호출
        resp, body, err := makeRequest("POST", "/v0.1/endpoint", reqBody, testToken)

        // Then: 검증
        assert.NoError(t, err)
        assertStatusCode(t, 200, resp.StatusCode, body)

        var result map[string]interface{}
        parseJSONResponse(t, body, &result)
        assert.Contains(t, result, "expected_field")
    })

    t.Run("2. 실패 케이스 - 필수 필드 누락", func(t *testing.T) {
        reqBody := map[string]interface{}{
            // 필수 필드 없음
        }

        resp, body, err := makeRequest("POST", "/v0.1/endpoint", reqBody, testToken)
        assert.NoError(t, err)
        assertStatusCode(t, 400, resp.StatusCode, body)
    })
}
```

#### Step 3: 로컬 테스트 실행
```bash
cd backend/src

# 전체 테스트 실행
./scripts/run_tests.sh all

# 특정 테스트만 실행
./scripts/run_tests.sh your-feature

# 또는 직접 go test
export DB_USER=root DB_PASSWORD=examplepassword \
       DB_HOST=13.203.37.93 DB_PORT=3306 \
       DB_NAME=daily_test GO_ENV=local
go test -v ./tests/e2e -run TestNewFeatureFlow
```

#### Step 4: 코드 리뷰 준비
```bash
# 커밋 전 체크리스트
✅ 기능 코드 구현 완료
✅ E2E 테스트 작성 완료
✅ 모든 테스트 통과 확인
✅ README 또는 API 문서 업데이트

# 커밋
git add .
git commit -m "feat: implement new feature with E2E tests"
```

#### Step 5: Pull Request
```bash
git push origin feature/your-feature-name

# GitHub에서 PR 생성
# PR 템플릿에 다음 항목 포함:
# - [ ] E2E 테스트 작성 완료
# - [ ] 로컬에서 모든 테스트 통과
# - [ ] API 문서 업데이트 (필요 시)
```

### 3. PR 승인 조건

다음 조건을 **모두** 만족해야 PR이 승인됩니다:

- ✅ **CI/CD 테스트 통과** (자동 실행)
- ✅ **E2E 테스트 포함** (코드 리뷰에서 확인)
- ✅ **기존 테스트 깨지지 않음**
- ✅ **코드 리뷰 승인**

### 4. 데이터베이스 스키마 변경

#### 마이그레이션 파일 작성

```bash
# 위치: backend/src/common/db/mysql/
cd backend/src/common/db/mysql

# 마이그레이션 파일 생성
vim migration_add_your_feature.sql
```

**마이그레이션 파일 예시:**
```sql
-- Migration: Add your feature
-- Created: 2025-11-06
-- Description: Add new table/column for feature

USE daily_dev;

-- 1. Add new column
ALTER TABLE table_name
ADD COLUMN new_column VARCHAR(100) COMMENT 'column description';

-- 2. Add index if needed
ALTER TABLE table_name
ADD INDEX idx_new_column (new_column);
```

#### CI/CD 스키마 동기화

`deploy-backend.yml`의 "Update test database schema" 단계에 추가:

```yaml
- name: Update test database schema
  run: |
    # 기존 마이그레이션...

    # 새 마이그레이션 추가
    docker run -i --rm mysql:8.0 mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD daily_test <<'EOF'
      -- Your migration SQL here
    EOF
```

### 5. 테스트 작성 가이드

#### 좋은 테스트의 조건

1. **독립성**: 각 테스트는 다른 테스트에 의존하지 않음
2. **반복성**: 몇 번을 실행해도 같은 결과
3. **명확성**: 테스트 이름으로 무엇을 검증하는지 알 수 있음
4. **완전성**: 성공 케이스 + 실패 케이스 모두 포함

#### 테스트 시나리오 체크리스트

- ✅ **성공 케이스**: 정상 요청 → 200/201 응답
- ✅ **검증 실패**: 잘못된 데이터 → 400 응답
- ✅ **인증 실패**: 토큰 없음/만료 → 401 응답
- ✅ **권한 없음**: 다른 사용자 데이터 접근 → 403 응답
- ✅ **리소스 없음**: 존재하지 않는 ID → 404 응답
- ✅ **중복 생성**: 이미 존재하는 데이터 → 409 응답

#### 테스트 네이밍 규칙

```go
// ✅ Good: 명확하고 구체적
t.Run("사용자가 유효한 토큰으로 메모를 생성하면 201을 반환한다", func(t *testing.T) {})
t.Run("필수 필드가 누락되면 400 에러를 반환한다", func(t *testing.T) {})
t.Run("다른 사용자의 메모를 수정하려고 하면 403 에러를 반환한다", func(t *testing.T) {})

// ❌ Bad: 모호함
t.Run("test1", func(t *testing.T) {})
t.Run("메모 생성", func(t *testing.T) {})
```

### 6. 자주 하는 실수

#### ❌ 하지 말아야 할 것

1. **테스트 없이 PR 생성**
   ```
   ❌ "테스트는 나중에 추가하겠습니다"
   ✅ PR 생성 전에 테스트 완료
   ```

2. **하드코딩된 데이터 사용**
   ```go
   ❌ userID := 123  // 하드코딩
   ✅ userID := testUserID  // 테스트에서 생성된 ID
   ```

3. **운영 DB로 테스트**
   ```bash
   ❌ DB_NAME=daily_dev go test
   ✅ DB_NAME=daily_test go test
   ```

4. **실패하는 테스트 커밋**
   ```bash
   ❌ git commit -m "WIP: tests failing"
   ✅ 모든 테스트 통과 후 커밋
   ```

5. **테스트 스킵/주석 처리**
   ```go
   ❌ t.Skip("TODO: fix this later")
   ❌ // t.Run("broken test", ...)
   ✅ 테스트 수정 후 실행
   ```

### 7. 도움말

#### 테스트 작성이 막힐 때

1. **기존 테스트 참고**: `tests/e2e/` 디렉토리의 다른 테스트 파일 확인
2. **README 확인**: `tests/e2e/README.md` 참고
3. **팀원에게 질문**: 막히면 바로 질문하기

#### 유용한 명령어

```bash
# 특정 테스트만 실행
go test -v ./tests/e2e -run TestAuthFlow

# 실패한 테스트만 재실행
go test -v ./tests/e2e -failfast

# 테스트 커버리지 확인
go test -v -coverprofile=coverage.out ./tests/e2e/...
go tool cover -html=coverage.out
```

### 8. 예외 사항

다음 경우에만 테스트 없이 PR 가능:

- 📝 문서 수정만 있는 경우
- 🎨 UI/스타일 변경만 있는 경우 (로직 변경 없음)
- 🔧 설정 파일 수정 (CI/CD 제외)
- 🐛 긴급 핫픽스 (테스트는 별도 PR로 후속 작업)

**긴급 핫픽스의 경우:**
```bash
# 1. 긴급 수정 먼저
git commit -m "hotfix: critical bug fix"
git push

# 2. 테스트 추가 (즉시)
git commit -m "test: add E2E test for hotfix"
git push
```

## 문의

질문이나 제안사항이 있으면 팀 채널에 공유해주세요!
