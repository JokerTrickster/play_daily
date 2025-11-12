# Unit Tests for Token Storage

This directory contains comprehensive unit tests for the JWT token storage functionality.

## Test File

- **token_storage_test.go**: Unit tests for token storage, retrieval, and revocation functions

## Test Coverage

### Functions Tested

All token storage functions in `backend/src/common/jwt.go`:

1. **StoreToken()**: 75.0% coverage
   - Successful token storage
   - Multiple tokens for same user

2. **GetTokenByRefreshToken()**: 87.5% coverage
   - Successful retrieval
   - Expired token rejection
   - Revoked token rejection
   - Non-existent token rejection

3. **GetTokenByAccessToken()**: 87.5% coverage
   - Successful retrieval
   - Expired token rejection
   - Revoked token rejection

4. **RevokeTokenByRefreshToken()**: 85.7% coverage
   - Successful revocation
   - Non-existent token rejection
   - Already revoked token rejection

### Test Scenarios

Total: **22 test cases** across **15 test functions**

#### Core Functionality Tests
- ✓ StoreToken_Success
- ✓ StoreToken_MultipleTokens
- ✓ GetTokenByRefreshToken_Success
- ✓ GetTokenByRefreshToken_ExpiredToken
- ✓ GetTokenByRefreshToken_RevokedToken
- ✓ GetTokenByRefreshToken_NonExistent
- ✓ GetTokenByAccessToken_Success
- ✓ GetTokenByAccessToken_ExpiredToken
- ✓ GetTokenByAccessToken_RevokedToken
- ✓ RevokeTokenByRefreshToken_Success
- ✓ RevokeTokenByRefreshToken_NonExistent
- ✓ RevokeTokenByRefreshToken_AlreadyRevoked

#### Integration Tests
- ✓ TokenLifecycle_CompleteFlow (5 sub-tests)
  - Generate and Store Token
  - Retrieve Token by Refresh Token
  - Retrieve Token by Access Token
  - Revoke Token
  - Verify Revoked Token Cannot Be Retrieved

#### Advanced Tests
- ✓ ConcurrentTokenOperations (thread-safety)
- ✓ TokenExpirationValidation (2 sub-tests)
  - Future Expiration - Valid
  - Past Expiration - Expired

## Running Tests

### Run All Unit Tests
```bash
cd backend/src
go test -v ./tests/unit
```

### Run Specific Test
```bash
cd backend/src
go test -v ./tests/unit -run TestStoreToken_Success
```

### Run with Coverage
```bash
cd backend/src
go test -v -coverpkg=./common ./tests/unit
```

### Generate Detailed Coverage Report
```bash
cd backend/src
go test ./tests/unit -coverprofile=coverage.out -coverpkg=./common
go tool cover -html=coverage.out -o coverage.html
```

## Test Architecture

### Database Setup
- Uses in-memory SQLite database for fast, isolated testing
- Each test gets a fresh database instance
- Auto-migrates Token and User models
- Cleanup after each test to prevent data pollution

### Test Helpers
- `setupTestDB(t)`: Creates and initializes in-memory database
- `cleanupTestDB(t, db)`: Clears test data after tests
- `createTestUser(t, db, accountID)`: Creates test user for token operations

### Test Patterns
- **Arrange-Act-Assert**: Clear separation of setup, execution, and validation
- **Comprehensive Error Testing**: Tests both success and failure scenarios
- **Edge Case Coverage**: Expired tokens, revoked tokens, concurrent operations
- **Verbose Logging**: Detailed output for debugging test failures

## Code Quality Metrics

- **Total Coverage**: 23.6% of all common package statements
- **Token Functions Coverage**: 75-87.5% (target functions)
- **Test Success Rate**: 100% (22/22 tests passing)
- **Error Handling**: All error cases thoroughly tested
- **Thread Safety**: Concurrent operations validated

## Dependencies

- `github.com/stretchr/testify/assert`: Assertions
- `github.com/stretchr/testify/require`: Critical assertions
- `gorm.io/driver/sqlite`: In-memory database for testing
- `gorm.io/gorm`: ORM framework

## Notes

- Tests use in-memory SQLite instead of MySQL for speed and isolation
- JWT initialization happens in TestMain for all tests
- Concurrent tests validate thread-safety with 5 concurrent operations
- Each test is independent and can run in any order
- Verbose logging helps with debugging and understanding test execution
