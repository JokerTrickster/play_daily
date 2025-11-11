package repository

import (
	"context"
	"crypto/rand"
	"errors"
	"main/common/db/mysql"
	_interface "main/features/room/model/interface"
	"math/big"
	"time"

	"gorm.io/gorm"
)

type ResetPasswordRepository struct {
	DB *gorm.DB
}

func NewResetPasswordRepository(db *gorm.DB) _interface.IResetPasswordRepository {
	return &ResetPasswordRepository{
		DB: db,
	}
}

// GenerateSecurePassword generates a cryptographically secure 12-character password
// Character set: [A-Za-z0-9!@#$%^&*]
// Requirements: At least 1 uppercase, 1 lowercase, 1 digit, 1 special character
func (r *ResetPasswordRepository) GenerateSecurePassword(ctx context.Context) (string, error) {
	const (
		uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
		lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
		digitChars     = "0123456789"
		specialChars   = "!@#$%^&*"
		allChars       = uppercaseChars + lowercaseChars + digitChars + specialChars
		passwordLength = 12
	)

	// Ensure minimum requirements: 1 of each type
	password := make([]byte, passwordLength)

	// Get one character from each required set
	char, err := getSecureRandomChar(uppercaseChars)
	if err != nil {
		return "", err
	}
	password[0] = char

	char, err = getSecureRandomChar(lowercaseChars)
	if err != nil {
		return "", err
	}
	password[1] = char

	char, err = getSecureRandomChar(digitChars)
	if err != nil {
		return "", err
	}
	password[2] = char

	char, err = getSecureRandomChar(specialChars)
	if err != nil {
		return "", err
	}
	password[3] = char

	// Fill remaining characters from all available characters
	for i := 4; i < passwordLength; i++ {
		char, err := getSecureRandomChar(allChars)
		if err != nil {
			return "", err
		}
		password[i] = char
	}

	// Shuffle the password to avoid predictable pattern
	if err := shuffleBytes(password); err != nil {
		return "", err
	}

	return string(password), nil
}

// getSecureRandomChar returns a cryptographically secure random character from the given charset
func getSecureRandomChar(charset string) (byte, error) {
	max := big.NewInt(int64(len(charset)))
	n, err := rand.Int(rand.Reader, max)
	if err != nil {
		return 0, err
	}
	return charset[n.Int64()], nil
}

// shuffleBytes shuffles a byte slice using cryptographically secure random numbers
func shuffleBytes(data []byte) error {
	n := len(data)
	for i := n - 1; i > 0; i-- {
		jBig, err := rand.Int(rand.Reader, big.NewInt(int64(i+1)))
		if err != nil {
			return err
		}
		j := int(jBig.Int64())
		data[i], data[j] = data[j], data[i]
	}
	return nil
}

// CheckRoomOwner verifies if the user is the owner of the room
func (r *ResetPasswordRepository) CheckRoomOwner(ctx context.Context, roomID uint, userID uint) (bool, error) {
	var count int64
	err := r.DB.WithContext(ctx).
		Model(&mysql.Room{}).
		Where("id = ? AND owner_user_id = ? AND deleted_at IS NULL", roomID, userID).
		Count(&count).Error

	if err != nil {
		return false, err
	}
	return count > 0, nil
}

// GetRoomPassword retrieves the current password of the room
func (r *ResetPasswordRepository) GetRoomPassword(ctx context.Context, roomID uint) (string, error) {
	var room mysql.Room
	err := r.DB.WithContext(ctx).
		Select("room_password").
		Where("id = ? AND deleted_at IS NULL", roomID).
		First(&room).Error

	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return "", errors.New("room not found")
		}
		return "", err
	}

	return room.RoomPassword, nil
}

// CheckRecentResets checks if there have been more than the allowed resets in the time window
func (r *ResetPasswordRepository) CheckRecentResets(ctx context.Context, roomID uint, limit int, windowHours int) (int, error) {
	var count int64
	cutoffTime := time.Now().Add(-time.Duration(windowHours) * time.Hour)

	err := r.DB.WithContext(ctx).
		Model(&mysql.RoomPasswordReset{}).
		Where("room_id = ? AND reset_at >= ?", roomID, cutoffTime).
		Count(&count).Error

	if err != nil {
		return 0, err
	}

	return int(count), nil
}

// ResetPassword updates the room password and logs the reset in a transaction
func (r *ResetPasswordRepository) ResetPassword(ctx context.Context, roomID uint, userID uint, newPassword string, previousPassword string, ipAddress *string, userAgent *string) error {
	return r.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// 1. Update room password
		result := tx.Model(&mysql.Room{}).
			Where("id = ? AND deleted_at IS NULL", roomID).
			Update("room_password", newPassword)

		if result.Error != nil {
			return result.Error
		}

		if result.RowsAffected == 0 {
			return errors.New("room not found or already deleted")
		}

		// 2. Create audit log entry
		resetLog := mysql.RoomPasswordReset{
			RoomID:               roomID,
			ResetByUserID:        &userID,
			ResetAt:              time.Now(),
			PreviousPasswordHash: previousPassword, // Note: Currently storing plaintext as hash field
			NewPasswordHash:      newPassword,      // Note: Currently storing plaintext as hash field
			IPAddress:            ipAddress,
			UserAgent:            userAgent,
		}

		if err := tx.Create(&resetLog).Error; err != nil {
			return err
		}

		return nil
	})
}

// LogRateLimitViolation logs rate limit violations for security monitoring
func (r *ResetPasswordRepository) LogRateLimitViolation(ctx context.Context, roomID uint, userID uint, ipAddress *string, userAgent *string) error {
	// For now, we'll create a special audit log entry with empty password hashes to indicate rate limit violation
	// In production, you might want a separate rate_limit_violations table
	resetLog := mysql.RoomPasswordReset{
		RoomID:               roomID,
		ResetByUserID:        &userID,
		ResetAt:              time.Now(),
		PreviousPasswordHash: "RATE_LIMIT_VIOLATION",
		NewPasswordHash:      "RATE_LIMIT_VIOLATION",
		IPAddress:            ipAddress,
		UserAgent:            userAgent,
	}

	return r.DB.WithContext(ctx).Create(&resetLog).Error
}
