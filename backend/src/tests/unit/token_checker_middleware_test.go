package unit

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"main/common"
	"main/common/db/mysql"
	_middleware "main/middleware"
)

// setupMiddlewareTestDB initializes an in-memory SQLite database for middleware testing
func setupMiddlewareTestDB(t *testing.T) *gorm.DB {
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	require.NoError(t, err, "Failed to create in-memory database")

	// Migrate schema
	err = db.AutoMigrate(&mysql.User{}, &mysql.Token{})
	require.NoError(t, err, "Failed to migrate database schema")

	return db
}

// cleanupMiddlewareTestDB clears all data from test database
func cleanupMiddlewareTestDB(db *gorm.DB) {
	db.Exec("DELETE FROM tokens")
	db.Exec("DELETE FROM users")
}

// createMiddlewareTestUser creates a test user in the database
func createMiddlewareTestUser(t *testing.T, db *gorm.DB, accountID, password, nickname string) *mysql.User {
	user := &mysql.User{
		AccountID:    accountID,
		Password:     password,
		Nickname:     nickname,
		RoomPassword: "1234",
	}
	err := db.Create(user).Error
	require.NoError(t, err, "Failed to create test user")
	return user
}

func TestTokenChecker_ValidToken(t *testing.T) {
	// Setup
	db := setupMiddlewareTestDB(t)
	defer cleanupMiddlewareTestDB(db)

	// Initialize JWT
	err := common.InitJwt()
	require.NoError(t, err)

	// Create test user
	user := createMiddlewareTestUser(t, db, "test@example.com", "password123", "TestUser")

	// Generate valid token
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken(user.AccountID, user.ID)
	require.NoError(t, err)

	// Store token in database
	err = common.StoreToken(db, user.ID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Mock global DB connection
	originalDB := mysql.GormMysqlDB
	mysql.GormMysqlDB = db
	defer func() { mysql.GormMysqlDB = originalDB }()

	// Create Echo context
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("tkn", accessToken)
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)

	// Create handler that will be called if middleware passes
	handlerCalled := false
	handler := func(c echo.Context) error {
		handlerCalled = true
		// Verify context values
		uID := c.Get("uID").(uint)
		email := c.Get("email").(string)
		assert.Equal(t, user.ID, uID)
		assert.Equal(t, user.AccountID, email)
		return nil
	}

	// Execute middleware
	middleware := _middleware.TokenChecker(handler)
	err = middleware(c)

	// Assert
	assert.NoError(t, err)
	assert.True(t, handlerCalled, "Handler should be called for valid token")
}

func TestTokenChecker_MissingToken(t *testing.T) {
	// Setup
	db := setupMiddlewareTestDB(t)
	defer cleanupMiddlewareTestDB(db)

	// Initialize JWT
	err := common.InitJwt()
	require.NoError(t, err)

	// Mock global DB connection
	originalDB := mysql.GormMysqlDB
	mysql.GormMysqlDB = db
	defer func() { mysql.GormMysqlDB = originalDB }()

	// Create Echo context without token
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)

	// Create handler that should not be called
	handlerCalled := false
	handler := func(c echo.Context) error {
		handlerCalled = true
		return nil
	}

	// Execute middleware
	middleware := _middleware.TokenChecker(handler)
	err = middleware(c)

	// Assert
	assert.Error(t, err)
	assert.False(t, handlerCalled, "Handler should not be called when token is missing")
	assert.Contains(t, err.Error(), "access token required")
}

func TestTokenChecker_InvalidJWTSignature(t *testing.T) {
	// Setup
	db := setupMiddlewareTestDB(t)
	defer cleanupMiddlewareTestDB(db)

	// Initialize JWT
	err := common.InitJwt()
	require.NoError(t, err)

	// Mock global DB connection
	originalDB := mysql.GormMysqlDB
	mysql.GormMysqlDB = db
	defer func() { mysql.GormMysqlDB = originalDB }()

	// Create Echo context with invalid token
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("tkn", "invalid.jwt.token")
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)

	// Create handler that should not be called
	handlerCalled := false
	handler := func(c echo.Context) error {
		handlerCalled = true
		return nil
	}

	// Execute middleware
	middleware := _middleware.TokenChecker(handler)
	err = middleware(c)

	// Assert
	assert.Error(t, err)
	assert.False(t, handlerCalled, "Handler should not be called for invalid JWT")
	assert.Contains(t, err.Error(), "failed to parse token")
}

func TestTokenChecker_TokenNotInDatabase(t *testing.T) {
	// Setup
	db := setupMiddlewareTestDB(t)
	defer cleanupMiddlewareTestDB(db)

	// Initialize JWT
	err := common.InitJwt()
	require.NoError(t, err)

	// Create test user
	user := createMiddlewareTestUser(t, db, "test@example.com", "password123", "TestUser")

	// Generate valid token but DON'T store in database
	accessToken, _, _, _, err := common.GenerateToken(user.AccountID, user.ID)
	require.NoError(t, err)

	// Mock global DB connection
	originalDB := mysql.GormMysqlDB
	mysql.GormMysqlDB = db
	defer func() { mysql.GormMysqlDB = originalDB }()

	// Create Echo context
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("tkn", accessToken)
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)

	// Create handler that should not be called
	handlerCalled := false
	handler := func(c echo.Context) error {
		handlerCalled = true
		return nil
	}

	// Execute middleware
	middleware := _middleware.TokenChecker(handler)
	err = middleware(c)

	// Assert
	assert.Error(t, err)
	assert.False(t, handlerCalled, "Handler should not be called when token not in database")
	assert.Contains(t, err.Error(), "token not found or revoked")
}

func TestTokenChecker_RevokedToken(t *testing.T) {
	// Setup
	db := setupMiddlewareTestDB(t)
	defer cleanupMiddlewareTestDB(db)

	// Initialize JWT
	err := common.InitJwt()
	require.NoError(t, err)

	// Create test user
	user := createMiddlewareTestUser(t, db, "test@example.com", "password123", "TestUser")

	// Generate valid token
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken(user.AccountID, user.ID)
	require.NoError(t, err)

	// Store token in database
	err = common.StoreToken(db, user.ID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Revoke the token
	err = common.RevokeTokenByRefreshToken(db, refreshToken)
	require.NoError(t, err)

	// Mock global DB connection
	originalDB := mysql.GormMysqlDB
	mysql.GormMysqlDB = db
	defer func() { mysql.GormMysqlDB = originalDB }()

	// Create Echo context
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("tkn", accessToken)
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)

	// Create handler that should not be called
	handlerCalled := false
	handler := func(c echo.Context) error {
		handlerCalled = true
		return nil
	}

	// Execute middleware
	middleware := _middleware.TokenChecker(handler)
	err = middleware(c)

	// Assert
	assert.Error(t, err)
	assert.False(t, handlerCalled, "Handler should not be called for revoked token")
	assert.Contains(t, err.Error(), "token not found or revoked")
}

func TestTokenChecker_ExpiredToken(t *testing.T) {
	// Setup
	db := setupMiddlewareTestDB(t)
	defer cleanupMiddlewareTestDB(db)

	// Initialize JWT
	err := common.InitJwt()
	require.NoError(t, err)

	// Create test user
	user := createMiddlewareTestUser(t, db, "test@example.com", "password123", "TestUser")

	// Generate token with past expiration
	accessToken, _, refreshToken, _, err := common.GenerateToken(user.AccountID, user.ID)
	require.NoError(t, err)

	// Store token with past expiration time
	pastTime := time.Now().Add(-1 * time.Hour)
	err = common.StoreToken(db, user.ID, accessToken, refreshToken, pastTime, pastTime)
	require.NoError(t, err)

	// Mock global DB connection
	originalDB := mysql.GormMysqlDB
	mysql.GormMysqlDB = db
	defer func() { mysql.GormMysqlDB = originalDB }()

	// Create Echo context
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("tkn", accessToken)
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)

	// Create handler that should not be called
	handlerCalled := false
	handler := func(c echo.Context) error {
		handlerCalled = true
		return nil
	}

	// Execute middleware
	middleware := _middleware.TokenChecker(handler)
	err = middleware(c)

	// Assert
	assert.Error(t, err)
	assert.False(t, handlerCalled, "Handler should not be called for expired token")
	assert.Contains(t, err.Error(), "access token expired")
}

func TestTokenChecker_UserIDMismatch(t *testing.T) {
	// Setup
	db := setupMiddlewareTestDB(t)
	defer cleanupMiddlewareTestDB(db)

	// Initialize JWT
	err := common.InitJwt()
	require.NoError(t, err)

	// Create two test users
	user1 := createMiddlewareTestUser(t, db, "user1@example.com", "password123", "User1")
	user2 := createMiddlewareTestUser(t, db, "user2@example.com", "password456", "User2")

	// Generate token for user1
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken(user1.AccountID, user1.ID)
	require.NoError(t, err)

	// Store token with user2's ID (mismatch scenario)
	err = common.StoreToken(db, user2.ID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Mock global DB connection
	originalDB := mysql.GormMysqlDB
	mysql.GormMysqlDB = db
	defer func() { mysql.GormMysqlDB = originalDB }()

	// Create Echo context
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("tkn", accessToken)
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)

	// Create handler that should not be called
	handlerCalled := false
	handler := func(c echo.Context) error {
		handlerCalled = true
		return nil
	}

	// Execute middleware
	middleware := _middleware.TokenChecker(handler)
	err = middleware(c)

	// Assert
	assert.Error(t, err)
	assert.False(t, handlerCalled, "Handler should not be called for user ID mismatch")
	assert.Contains(t, err.Error(), "token user mismatch")
}

// Benchmark test for middleware performance
func BenchmarkTokenChecker_ValidToken(b *testing.B) {
	// Setup
	db, _ := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	db.AutoMigrate(&mysql.User{}, &mysql.Token{})

	// Initialize JWT
	common.InitJwt()

	// Create test user
	user := &mysql.User{
		AccountID:    "bench@example.com",
		Password:     "password",
		Nickname:     "BenchUser",
		RoomPassword: "1234",
	}
	db.Create(user)

	// Generate and store token
	accessToken, accessExp, refreshToken, refreshExp, _ := common.GenerateToken(user.AccountID, user.ID)
	common.StoreToken(db, user.ID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))

	// Mock global DB connection
	originalDB := mysql.GormMysqlDB
	mysql.GormMysqlDB = db
	defer func() { mysql.GormMysqlDB = originalDB }()

	// Create Echo context
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/test", nil)
	req.Header.Set("tkn", accessToken)
	rec := httptest.NewRecorder()

	handler := func(c echo.Context) error {
		return nil
	}

	middleware := _middleware.TokenChecker(handler)

	// Reset timer and run benchmark
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		c := e.NewContext(req, rec)
		middleware(c)
	}
}
