package unit

import (
	"fmt"
	"main/common"
	"main/common/db/mysql"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
)

var testDB *gorm.DB

// setupTestDB initializes an in-memory SQLite database for testing
func setupTestDB(t *testing.T) *gorm.DB {
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	require.NoError(t, err, "Failed to create in-memory database")

	// Auto-migrate the Token and User models
	err = db.AutoMigrate(&mysql.Token{}, &mysql.User{})
	require.NoError(t, err, "Failed to migrate database schema")

	return db
}

// cleanupTestDB clears all data from test database
func cleanupTestDB(t *testing.T, db *gorm.DB) {
	db.Exec("DELETE FROM tokens")
	db.Exec("DELETE FROM users")
}

// createTestUser creates a test user in the database
func createTestUser(t *testing.T, db *gorm.DB, accountID string) uint {
	user := &mysql.User{
		AccountID:    accountID,
		Password:     "hashed_password",
		Nickname:     "Test User",
		RoomPassword: "1234",
	}
	err := db.Create(user).Error
	require.NoError(t, err, "Failed to create test user")
	return user.ID
}

// TestMain sets up test environment
func TestMain(m *testing.M) {
	// Initialize JWT for token generation
	common.InitJwt()
	m.Run()
}

// TestStoreToken_Success tests successful token storage
func TestStoreToken_Success(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	// Create test user
	userID := createTestUser(t, db, "test@example.com")

	// Generate tokens
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err, "Failed to generate tokens")

	// Convert unix timestamps to time.Time
	accessExpTime := time.Unix(accessExp, 0)
	refreshExpTime := time.Unix(refreshExp, 0)

	// Store tokens
	err = common.StoreToken(db, userID, accessToken, refreshToken, accessExpTime, refreshExpTime)
	assert.NoError(t, err, "StoreToken should succeed")

	// Verify token was stored
	var storedToken mysql.Token
	err = db.Where("user_id = ?", userID).First(&storedToken).Error
	assert.NoError(t, err, "Token should be found in database")
	assert.Equal(t, userID, storedToken.UserID, "User ID should match")
	assert.Equal(t, accessToken, storedToken.AccessToken, "Access token should match")
	assert.Equal(t, refreshToken, storedToken.RefreshToken, "Refresh token should match")
	assert.Nil(t, storedToken.RevokedAt, "RevokedAt should be nil for new token")

	t.Logf("✓ Token stored successfully - UserID: %d, TokenID: %d", userID, storedToken.ID)
}

// TestStoreToken_MultipleTokens tests storing multiple tokens for same user
func TestStoreToken_MultipleTokens(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Store first token
	accessToken1, accessExp1, refreshToken1, refreshExp1, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)
	err = common.StoreToken(db, userID, accessToken1, refreshToken1, time.Unix(accessExp1, 0), time.Unix(refreshExp1, 0))
	assert.NoError(t, err)

	// Store second token (different session)
	time.Sleep(10 * time.Millisecond) // Ensure different timestamp
	accessToken2, accessExp2, refreshToken2, refreshExp2, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)
	err = common.StoreToken(db, userID, accessToken2, refreshToken2, time.Unix(accessExp2, 0), time.Unix(refreshExp2, 0))
	assert.NoError(t, err)

	// Verify both tokens exist
	var count int64
	db.Model(&mysql.Token{}).Where("user_id = ?", userID).Count(&count)
	assert.Equal(t, int64(2), count, "Should have 2 tokens stored for user")

	t.Logf("✓ Multiple tokens stored successfully - UserID: %d, Count: %d", userID, count)
}

// TestGetTokenByRefreshToken_Success tests successful token retrieval by refresh token
func TestGetTokenByRefreshToken_Success(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Store token
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)
	err = common.StoreToken(db, userID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Retrieve token by refresh token
	retrievedToken, err := common.GetTokenByRefreshToken(db, refreshToken)
	assert.NoError(t, err, "GetTokenByRefreshToken should succeed")
	assert.NotNil(t, retrievedToken, "Retrieved token should not be nil")
	assert.Equal(t, userID, retrievedToken.UserID, "User ID should match")
	assert.Equal(t, accessToken, retrievedToken.AccessToken, "Access token should match")
	assert.Equal(t, refreshToken, retrievedToken.RefreshToken, "Refresh token should match")
	assert.Nil(t, retrievedToken.RevokedAt, "RevokedAt should be nil")

	t.Logf("✓ Token retrieved successfully by refresh token - TokenID: %d", retrievedToken.ID)
}

// TestGetTokenByRefreshToken_ExpiredToken tests retrieval of expired token
func TestGetTokenByRefreshToken_ExpiredToken(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Generate and store token with expired refresh token
	accessToken, _, refreshToken, _, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)

	// Set expiration times in the past
	expiredTime := time.Now().Add(-1 * time.Hour)
	err = common.StoreToken(db, userID, accessToken, refreshToken, expiredTime, expiredTime)
	require.NoError(t, err)

	// Attempt to retrieve expired token
	retrievedToken, err := common.GetTokenByRefreshToken(db, refreshToken)
	assert.Error(t, err, "GetTokenByRefreshToken should return error for expired token")
	assert.Nil(t, retrievedToken, "Retrieved token should be nil for expired token")
	assert.Contains(t, err.Error(), "refresh token expired", "Error should indicate token expiration")

	t.Logf("✓ Expired token correctly rejected - Error: %v", err)
}

// TestGetTokenByRefreshToken_RevokedToken tests retrieval of revoked token
func TestGetTokenByRefreshToken_RevokedToken(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Store token
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)
	err = common.StoreToken(db, userID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Revoke the token
	err = common.RevokeTokenByRefreshToken(db, refreshToken)
	require.NoError(t, err, "Token revocation should succeed")

	// Attempt to retrieve revoked token
	retrievedToken, err := common.GetTokenByRefreshToken(db, refreshToken)
	assert.Error(t, err, "GetTokenByRefreshToken should return error for revoked token")
	assert.Nil(t, retrievedToken, "Retrieved token should be nil for revoked token")
	assert.Contains(t, err.Error(), "token not found or revoked", "Error should indicate token is revoked")

	t.Logf("✓ Revoked token correctly rejected - Error: %v", err)
}

// TestGetTokenByRefreshToken_NonExistent tests retrieval with non-existent refresh token
func TestGetTokenByRefreshToken_NonExistent(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	// Attempt to retrieve with non-existent refresh token
	fakeRefreshToken := "non.existent.token"
	retrievedToken, err := common.GetTokenByRefreshToken(db, fakeRefreshToken)
	assert.Error(t, err, "GetTokenByRefreshToken should return error for non-existent token")
	assert.Nil(t, retrievedToken, "Retrieved token should be nil")
	assert.Contains(t, err.Error(), "token not found or revoked", "Error should indicate token not found")

	t.Logf("✓ Non-existent token correctly rejected - Error: %v", err)
}

// TestGetTokenByAccessToken_Success tests successful token retrieval by access token
func TestGetTokenByAccessToken_Success(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Store token
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)
	err = common.StoreToken(db, userID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Retrieve token by access token
	retrievedToken, err := common.GetTokenByAccessToken(db, accessToken)
	assert.NoError(t, err, "GetTokenByAccessToken should succeed")
	assert.NotNil(t, retrievedToken, "Retrieved token should not be nil")
	assert.Equal(t, userID, retrievedToken.UserID, "User ID should match")
	assert.Equal(t, accessToken, retrievedToken.AccessToken, "Access token should match")
	assert.Equal(t, refreshToken, retrievedToken.RefreshToken, "Refresh token should match")
	assert.Nil(t, retrievedToken.RevokedAt, "RevokedAt should be nil")

	t.Logf("✓ Token retrieved successfully by access token - TokenID: %d", retrievedToken.ID)
}

// TestGetTokenByAccessToken_ExpiredToken tests retrieval of expired access token
func TestGetTokenByAccessToken_ExpiredToken(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Generate and store token with expired access token
	accessToken, _, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)

	// Set access token expiration in the past
	expiredTime := time.Now().Add(-1 * time.Hour)
	err = common.StoreToken(db, userID, accessToken, refreshToken, expiredTime, time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Attempt to retrieve expired access token
	retrievedToken, err := common.GetTokenByAccessToken(db, accessToken)
	assert.Error(t, err, "GetTokenByAccessToken should return error for expired token")
	assert.Nil(t, retrievedToken, "Retrieved token should be nil for expired token")
	assert.Contains(t, err.Error(), "access token expired", "Error should indicate token expiration")

	t.Logf("✓ Expired access token correctly rejected - Error: %v", err)
}

// TestGetTokenByAccessToken_RevokedToken tests retrieval of revoked token by access token
func TestGetTokenByAccessToken_RevokedToken(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Store token
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)
	err = common.StoreToken(db, userID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Revoke the token
	err = common.RevokeTokenByRefreshToken(db, refreshToken)
	require.NoError(t, err, "Token revocation should succeed")

	// Attempt to retrieve revoked token by access token
	retrievedToken, err := common.GetTokenByAccessToken(db, accessToken)
	assert.Error(t, err, "GetTokenByAccessToken should return error for revoked token")
	assert.Nil(t, retrievedToken, "Retrieved token should be nil for revoked token")
	assert.Contains(t, err.Error(), "token not found or revoked", "Error should indicate token is revoked")

	t.Logf("✓ Revoked token correctly rejected via access token - Error: %v", err)
}

// TestRevokeTokenByRefreshToken_Success tests successful token revocation
func TestRevokeTokenByRefreshToken_Success(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Store token
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)
	err = common.StoreToken(db, userID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Revoke the token
	err = common.RevokeTokenByRefreshToken(db, refreshToken)
	assert.NoError(t, err, "RevokeTokenByRefreshToken should succeed")

	// Verify token is revoked in database
	var token mysql.Token
	err = db.Where("refresh_token = ?", refreshToken).First(&token).Error
	assert.NoError(t, err, "Token should still exist in database")
	assert.NotNil(t, token.RevokedAt, "RevokedAt should be set")
	assert.True(t, time.Now().After(*token.RevokedAt) || time.Now().Equal(*token.RevokedAt),
		"RevokedAt should be in the past or now")

	t.Logf("✓ Token revoked successfully - TokenID: %d, RevokedAt: %v", token.ID, token.RevokedAt)
}

// TestRevokeTokenByRefreshToken_NonExistent tests revoking non-existent token
func TestRevokeTokenByRefreshToken_NonExistent(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	// Attempt to revoke non-existent token
	fakeRefreshToken := "non.existent.token"
	err := common.RevokeTokenByRefreshToken(db, fakeRefreshToken)
	assert.Error(t, err, "RevokeTokenByRefreshToken should return error for non-existent token")
	assert.Contains(t, err.Error(), "token not found or already revoked",
		"Error should indicate token not found")

	t.Logf("✓ Non-existent token revocation correctly rejected - Error: %v", err)
}

// TestRevokeTokenByRefreshToken_AlreadyRevoked tests revoking already revoked token
func TestRevokeTokenByRefreshToken_AlreadyRevoked(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	// Store token
	accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
	require.NoError(t, err)
	err = common.StoreToken(db, userID, accessToken, refreshToken, time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
	require.NoError(t, err)

	// Revoke the token first time
	err = common.RevokeTokenByRefreshToken(db, refreshToken)
	require.NoError(t, err, "First revocation should succeed")

	// Attempt to revoke again
	err = common.RevokeTokenByRefreshToken(db, refreshToken)
	assert.Error(t, err, "RevokeTokenByRefreshToken should return error for already revoked token")
	assert.Contains(t, err.Error(), "token not found or already revoked",
		"Error should indicate token already revoked")

	t.Logf("✓ Already revoked token correctly rejected - Error: %v", err)
}

// TestTokenLifecycle_CompleteFlow tests complete token lifecycle
func TestTokenLifecycle_CompleteFlow(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	t.Run("1. Generate and Store Token", func(t *testing.T) {
		accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
		require.NoError(t, err)

		err = common.StoreToken(db, userID, accessToken, refreshToken,
			time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
		assert.NoError(t, err, "Token storage should succeed")

		t.Logf("✓ Token generated and stored")
	})

	var storedRefreshToken string
	var storedAccessToken string

	t.Run("2. Retrieve Token by Refresh Token", func(t *testing.T) {
		var token mysql.Token
		err := db.Where("user_id = ?", userID).First(&token).Error
		require.NoError(t, err)
		storedRefreshToken = token.RefreshToken
		storedAccessToken = token.AccessToken

		retrievedToken, err := common.GetTokenByRefreshToken(db, storedRefreshToken)
		assert.NoError(t, err, "Token retrieval should succeed")
		assert.Equal(t, userID, retrievedToken.UserID)

		t.Logf("✓ Token retrieved by refresh token")
	})

	t.Run("3. Retrieve Token by Access Token", func(t *testing.T) {
		retrievedToken, err := common.GetTokenByAccessToken(db, storedAccessToken)
		assert.NoError(t, err, "Token retrieval by access token should succeed")
		assert.Equal(t, userID, retrievedToken.UserID)

		t.Logf("✓ Token retrieved by access token")
	})

	t.Run("4. Revoke Token", func(t *testing.T) {
		err := common.RevokeTokenByRefreshToken(db, storedRefreshToken)
		assert.NoError(t, err, "Token revocation should succeed")

		t.Logf("✓ Token revoked")
	})

	t.Run("5. Verify Revoked Token Cannot Be Retrieved", func(t *testing.T) {
		_, err := common.GetTokenByRefreshToken(db, storedRefreshToken)
		assert.Error(t, err, "Revoked token retrieval should fail")

		_, err = common.GetTokenByAccessToken(db, storedAccessToken)
		assert.Error(t, err, "Revoked token retrieval by access token should fail")

		t.Logf("✓ Revoked token correctly rejected on retrieval")
	})

	t.Logf("✓ Complete token lifecycle tested successfully")
}

// TestConcurrentTokenOperations tests thread-safety of token operations
func TestConcurrentTokenOperations(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	// Create multiple users first (before concurrent operations)
	userCount := 5
	userIDs := make([]uint, userCount)
	for i := 0; i < userCount; i++ {
		userID := createTestUser(t, db, fmt.Sprintf("user%d@example.com", i))
		userIDs[i] = userID
	}

	// Concurrently store tokens for all users
	done := make(chan error, userCount)
	for i, userID := range userIDs {
		go func(uid uint, index int) {
			defer func() {
				if r := recover(); r != nil {
					done <- fmt.Errorf("panic: %v", r)
					return
				}
			}()

			email := fmt.Sprintf("user%d@example.com", index)
			// Add small delay to ensure different timestamps
			time.Sleep(time.Duration(index) * time.Millisecond)

			accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken(email, uid)
			if err != nil {
				done <- err
				return
			}

			err = common.StoreToken(db, uid, accessToken, refreshToken,
				time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
			done <- err
		}(userID, i)
	}

	// Wait for all goroutines to complete and check errors
	errorCount := 0
	for i := 0; i < userCount; i++ {
		if err := <-done; err != nil {
			t.Logf("Error in goroutine: %v", err)
			errorCount++
		}
	}

	assert.Equal(t, 0, errorCount, "All concurrent operations should succeed")

	// Verify all tokens were stored
	var count int64
	db.Model(&mysql.Token{}).Count(&count)
	assert.Equal(t, int64(userCount), count, "All tokens should be stored")

	t.Logf("✓ Concurrent token operations completed - %d tokens stored", count)
}

// TestTokenExpirationValidation tests expiration time validation
func TestTokenExpirationValidation(t *testing.T) {
	db := setupTestDB(t)
	defer cleanupTestDB(t, db)

	userID := createTestUser(t, db, "test@example.com")

	t.Run("Future Expiration - Valid", func(t *testing.T) {
		accessToken, accessExp, refreshToken, refreshExp, err := common.GenerateToken("test@example.com", userID)
		require.NoError(t, err)

		err = common.StoreToken(db, userID, accessToken, refreshToken,
			time.Unix(accessExp, 0), time.Unix(refreshExp, 0))
		assert.NoError(t, err, "Token with future expiration should be stored")

		// Verify token is retrievable
		_, err = common.GetTokenByRefreshToken(db, refreshToken)
		assert.NoError(t, err, "Valid token should be retrievable")
	})

	t.Run("Past Expiration - Expired", func(t *testing.T) {
		// Generate new token with different timestamp
		time.Sleep(10 * time.Millisecond)
		accessToken2, _, refreshToken2, _, err := common.GenerateToken("test@example.com", userID)
		require.NoError(t, err)

		pastTime := time.Now().Add(-24 * time.Hour)
		err = common.StoreToken(db, userID, accessToken2, refreshToken2, pastTime, pastTime)
		assert.NoError(t, err, "Token storage should succeed even with past expiration")

		// Verify token is not retrievable due to expiration
		_, err = common.GetTokenByRefreshToken(db, refreshToken2)
		assert.Error(t, err, "Expired token should not be retrievable")
		if err != nil {
			assert.Contains(t, err.Error(), "expired", "Error should indicate expiration")
		}
	})

	t.Logf("✓ Token expiration validation tested successfully")
}
