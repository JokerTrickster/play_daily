package common

import (
	"context"
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/joho/godotenv"
	"github.com/labstack/echo/v4"
)

type Config struct {
	// Server Configuration
	Host  string
	Port  string
	Env   string
	Debug bool

	// Database Configuration
	DBHost     string
	DBPort     string
	DBName     string
	DBUser     string
	DBPassword string

	// JWT Configuration
	JWTSecret      string
	JWTExpireHours int

	// File Upload Configuration
	UploadPath  string
	MaxFileSize int64

	// AWS S3 Configuration
	AWSRegion          string
	AWSAccessKeyID     string
	AWSSecretAccessKey string
	S3BucketName       string
	S3Endpoint         string // Optional: for MinIO or custom S3-compatible services

	// CORS Configuration
	AllowedOrigins []string

	// Logging Configuration
	LogLevel string
	LogFile  string

	// Environment flags
	IsLocal bool
	IsDev   bool
	IsProd  bool

	// Legacy fields for backward compatibility
	GoogleClientID     string
	GoogleClientSecret string
}

// Env : Environment
var Env *Config

// 사용하는 환경 변수 네임 설정 함수
func InitVarNames() []string {
	result := make([]string, 0)
	result = append(result, "HOST")
	result = append(result, "PORT")
	result = append(result, "ENV")
	result = append(result, "DEBUG")
	result = append(result, "DB_HOST")
	result = append(result, "DB_PORT")
	result = append(result, "DB_NAME")
	result = append(result, "DB_USER")
	result = append(result, "DB_PASSWORD")
	result = append(result, "JWT_SECRET")
	result = append(result, "JWT_EXPIRE_HOURS")
	result = append(result, "UPLOAD_PATH")
	result = append(result, "MAX_FILE_SIZE")
	result = append(result, "ALLOWED_ORIGINS")
	result = append(result, "LOG_LEVEL")
	result = append(result, "LOG_FILE")
	return result
}

// LoadConfig 환경 변수 로드 함수
func LoadConfig() error {
	// .env 파일 로드 (존재하는 경우)
	if err := godotenv.Load(); err != nil {
		// .env 파일이 없어도 에러가 아님 (환경 변수에서 직접 읽음)
		fmt.Println("Warning: .env file not found, using environment variables")
	}

	Env = &Config{
		// Server Configuration
		Host:  getEnv("HOST", "localhost"),
		Port:  getEnv("PORT", "7001"),
		Env:   getEnv("ENV", "local"),
		Debug: getEnvAsBool("DEBUG", true),

		// Database Configuration
		DBHost:     getEnv("DB_HOST", "13.203.37.93"),
		DBPort:     getEnv("DB_PORT", "3306"),
		DBName:     getEnv("DB_NAME", "daily_dev"),
		DBUser:     getEnv("DB_USER", "root"),
		DBPassword: getEnv("DB_PASSWORD", "examplepassword"),

		// JWT Configuration
		JWTSecret:      getEnv("JWT_SECRET", "play-daily-secret-key-2024"),
		JWTExpireHours: getEnvAsInt("JWT_EXPIRE_HOURS", 24),

		// File Upload Configuration
		UploadPath:  getEnv("UPLOAD_PATH", "./uploads"),
		MaxFileSize: getEnvAsInt64("MAX_FILE_SIZE", 10485760), // 10MB

		// AWS S3 Configuration
		AWSRegion:          getEnv("AWS_REGION", "ap-northeast-2"),
		AWSAccessKeyID:     getEnv("AWS_ACCESS_KEY_ID", ""),
		AWSSecretAccessKey: getEnv("AWS_SECRET_ACCESS_KEY", ""),
		S3BucketName:       getEnv("S3_BUCKET_NAME", "daily-memo-dev"),
		S3Endpoint:         getEnv("S3_ENDPOINT", ""), // Optional

		// CORS Configuration
		AllowedOrigins: getEnvAsSlice("ALLOWED_ORIGINS", []string{"http://localhost:3000", "http://localhost:5173"}),

		// Logging Configuration
		LogLevel: getEnv("LOG_LEVEL", "debug"),
		LogFile:  getEnv("LOG_FILE", "logs/app.log"),

		// Legacy fields
		GoogleClientID:     getEnv("GOOGLE_CLIENT_ID", ""),
		GoogleClientSecret: getEnv("GOOGLE_CLIENT_SECRET", ""),
	}

	// Environment flags 설정
	Env.IsLocal = Env.Env == "local"
	Env.IsDev = Env.Env == "dev"
	Env.IsProd = Env.Env == "prod"

	return nil
}

// 사용할 환경 변수 값들 초기화해주는 함수 (legacy)
func InitEnv() error {
	return LoadConfig()
}

// 환경 변수 읽기 함수들
func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func getEnvAsBool(key string, defaultValue bool) bool {
	if value := os.Getenv(key); value != "" {
		if boolValue, err := strconv.ParseBool(value); err == nil {
			return boolValue
		}
	}
	return defaultValue
}

func getEnvAsInt(key string, defaultValue int) int {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.Atoi(value); err == nil {
			return intValue
		}
	}
	return defaultValue
}

func getEnvAsInt64(key string, defaultValue int64) int64 {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.ParseInt(value, 10, 64); err == nil {
			return intValue
		}
	}
	return defaultValue
}

func getEnvAsSlice(key string, defaultValue []string) []string {
	if value := os.Getenv(key); value != "" {
		return strings.Split(value, ",")
	}
	return defaultValue
}

// 설정 정보 출력 (디버그용)
func (c *Config) Print() {
	fmt.Printf("=== Configuration ===\n")
	fmt.Printf("Port: %s\n", c.Port)
	fmt.Printf("Environment: %s\n", c.Env)
	fmt.Printf("Debug: %t\n", c.Debug)
	fmt.Printf("Upload Path: %s\n", c.UploadPath)
	fmt.Printf("Max File Size: %d bytes\n", c.MaxFileSize)
	fmt.Printf("Allowed Origins: %v\n", c.AllowedOrigins)
	fmt.Printf("===================\n")
}

func envIsLocal(isLocal string) bool {
	if isLocal != "true" {
		return false
	} else {
		return true
	}
}

func getOSLookupEnv(envVarNames []string) (map[string]string, error) {
	result := map[string]string{}
	var ok bool
	for _, envVarName := range envVarNames {
		if result[envVarName], ok = os.LookupEnv(envVarName); !ok {
			return nil, fmt.Errorf("os lookup get failed")
		}
	}
	return result, nil
}

func TimeToEpochMillis(time time.Time) int64 {
	nanos := time.UnixNano()
	millis := nanos / 1000000
	return millis
}

func EpochToTime(date int64) time.Time {
	return time.Unix(date, 0)
}

func EpochToTimeMillis(t int64) time.Time {
	return time.Unix(t/1000, t%1000*1000000)
}

func CtxGenerate(c echo.Context) (context.Context, uint, string) {
	userID, _ := c.Get("uID").(uint)
	requestID, _ := c.Get("rID").(string)
	startTime, _ := c.Get("startTime").(time.Time)
	email, _ := c.Get("email").(string)
	req := c.Request()
	ctx := context.WithValue(req.Context(), "key", &CtxValues{
		Method:    req.Method,
		Url:       req.URL.Path,
		UserID:    userID,
		RequestID: requestID,
		StartTime: startTime,
		Email:     email,
	})
	return ctx, userID, email
}

type CtxValues struct {
	Method    string
	Url       string
	UserID    uint
	StartTime time.Time
	RequestID string
	Email     string
}
