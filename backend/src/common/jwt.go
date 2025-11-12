package common

import (
	"context"
	"fmt"
	"time"

	"github.com/golang-jwt/jwt"
	"gorm.io/gorm"
	"main/common/db/mysql"
)

type JwtCustomClaims struct {
	CreateTime int64  `json:"createTime"`
	UserID     uint   `json:"userID"`
	Email      string `json:"email"`
	jwt.StandardClaims
}

var AccessTokenSecretKey []byte
var RefreshTokenSecretKey []byte

const (
	AccessTokenExpiredTime  = 24      //hours
	RefreshTokenExpiredTime = 24 * 30 //hours
)

func InitJwt() error {
	secret := "secret"
	AccessTokenSecretKey = []byte(secret)
	RefreshTokenSecretKey = []byte(secret)
	return nil
}

func GenerateToken(email string, userID uint) (string, int64, string, int64, error) {
	now := time.Now()
	accessToken, accessTknExpiredAt, err := GenerateAccessToken(email, now, userID)
	if err != nil {
		return "", 0, "", 0, err
	}
	refreshToken, refreshTknExpiredAt, err := GenerateRefreshToken(email, now, userID)
	if err != nil {
		return "", 0, "", 0, err
	}
	return accessToken, accessTknExpiredAt, refreshToken, refreshTknExpiredAt, nil
}

func GenerateAccessToken(email string, now time.Time, userID uint) (string, int64, error) {
	// Set custom claims
	expiredAt := now.Add(time.Hour * AccessTokenExpiredTime).Unix()
	claims := &JwtCustomClaims{
		TimeToEpochMillis(now),
		userID,
		email,
		jwt.StandardClaims{
			ExpiresAt: expiredAt,
		},
	}

	// Create token with claims
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	// Generate encoded token and send it as response.
	accessToken, err := token.SignedString(AccessTokenSecretKey)
	if err != nil {
		return "", 0, err
	}
	return accessToken, expiredAt, nil
}

func GenerateRefreshToken(email string, now time.Time, userID uint) (string, int64, error) {
	expiredAt := now.Add(time.Hour * RefreshTokenExpiredTime).Unix()
	claims := &JwtCustomClaims{
		TimeToEpochMillis(now),
		userID,
		email,
		jwt.StandardClaims{
			ExpiresAt: expiredAt,
		},
	}

	// Create token with claims
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	// Generate encoded token and send it as response.
	refreshToken, err := token.SignedString(RefreshTokenSecretKey)
	if err != nil {
		return "", 0, ErrorMsg(context.TODO(), ErrBadToken, Trace(), fmt.Sprintf("failed to generate refresh token - %v", err), ErrFromInternal)
	}
	return refreshToken, expiredAt, nil
}
func VerifyToken(tokenString string) error {
	// Parse the token
	token, err := jwt.ParseWithClaims(tokenString, &JwtCustomClaims{}, func(token *jwt.Token) (interface{}, error) {
		return AccessTokenSecretKey, nil
	})
	if err != nil {
		return ErrorMsg(context.TODO(), ErrBadToken, Trace(), fmt.Sprintf("failed to parse token - %v err - %v", token, err.Error()), ErrFromClient)
	}

	// Check token validity
	if !token.Valid {
		return ErrorMsg(context.TODO(), ErrBadToken, Trace(), fmt.Sprintf("invalid token - %v ", token), ErrFromClient)
	}
	return nil
}
func ParseToken(tokenString string) (uint, string, error) {
	token, _ := jwt.ParseWithClaims(tokenString, &JwtCustomClaims{}, func(token *jwt.Token) (interface{}, error) {
		return AccessTokenSecretKey, nil
	})
	// if err != nil {
	// 	return 0, "", ErrorMsg(context.TODO(), ErrBadToken, Trace(), fmt.Sprintf("failed to parse token - %v", token), ErrFromClient)
	// }
	// Extract claims
	claims, ok := token.Claims.(*JwtCustomClaims)
	if !ok {
		return 0, "", ErrorMsg(context.TODO(), ErrBadToken, Trace(), fmt.Sprintf("failed to extract claims - %v", token), ErrFromClient)
	}
	fmt.Println("claims: ", claims)
	// Extract email and userID
	email := claims.Email
	userID := claims.UserID
	return userID, email, nil
}

// StoreToken stores access and refresh tokens in the database
func StoreToken(db *gorm.DB, userID uint, accessToken, refreshToken string, accessExp, refreshExp time.Time) error {
	token := &mysql.Token{
		UserID:                userID,
		AccessToken:           accessToken,
		RefreshToken:          refreshToken,
		AccessTokenExpiresAt:  accessExp,
		RefreshTokenExpiresAt: refreshExp,
		RevokedAt:             nil,
	}

	if err := db.Create(token).Error; err != nil {
		return ErrorMsg(context.TODO(), ErrInternalDB, Trace(), fmt.Sprintf("failed to store token - %v", err), ErrFromInternal)
	}

	return nil
}

// GetTokenByRefreshToken retrieves a token by refresh token
func GetTokenByRefreshToken(db *gorm.DB, refreshToken string) (*mysql.Token, error) {
	var token mysql.Token

	if err := db.Where("refresh_token = ? AND revoked_at IS NULL", refreshToken).First(&token).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, ErrorMsg(context.TODO(), ErrBadToken, Trace(), "token not found or revoked", ErrFromClient)
		}
		return nil, ErrorMsg(context.TODO(), ErrInternalDB, Trace(), fmt.Sprintf("failed to get token - %v", err), ErrFromInternal)
	}

	// Check if refresh token is expired
	if time.Now().After(token.RefreshTokenExpiresAt) {
		return nil, ErrorMsg(context.TODO(), ErrBadToken, Trace(), "refresh token expired", ErrFromClient)
	}

	return &token, nil
}

// GetTokenByAccessToken retrieves a token by access token
func GetTokenByAccessToken(db *gorm.DB, accessToken string) (*mysql.Token, error) {
	var token mysql.Token

	if err := db.Where("access_token = ? AND revoked_at IS NULL", accessToken).First(&token).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, ErrorMsg(context.TODO(), ErrBadToken, Trace(), "token not found or revoked", ErrFromClient)
		}
		return nil, ErrorMsg(context.TODO(), ErrInternalDB, Trace(), fmt.Sprintf("failed to get token - %v", err), ErrFromInternal)
	}

	// Check if access token is expired
	if time.Now().After(token.AccessTokenExpiresAt) {
		return nil, ErrorMsg(context.TODO(), ErrBadToken, Trace(), "access token expired", ErrFromClient)
	}

	return &token, nil
}

// RevokeTokenByRefreshToken revokes a token using refresh token
func RevokeTokenByRefreshToken(db *gorm.DB, refreshToken string) error {
	now := time.Now()

	result := db.Model(&mysql.Token{}).
		Where("refresh_token = ? AND revoked_at IS NULL", refreshToken).
		Update("revoked_at", now)

	if result.Error != nil {
		return ErrorMsg(context.TODO(), ErrInternalDB, Trace(), fmt.Sprintf("failed to revoke token - %v", result.Error), ErrFromInternal)
	}

	if result.RowsAffected == 0 {
		return ErrorMsg(context.TODO(), ErrBadToken, Trace(), "token not found or already revoked", ErrFromClient)
	}

	return nil
}
