package storage

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"mime/multipart"
	"path/filepath"
	"strings"
	"time"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	"github.com/google/uuid"
)

type S3Client struct {
	client     *s3.Client
	bucketName string
	region     string
}

type S3Config struct {
	Region          string
	AccessKeyID     string
	SecretAccessKey string
	BucketName      string
	Endpoint        string
}

var S3 *S3Client

// InitS3 S3 클라이언트 초기화
func InitS3(cfg S3Config) error {
	ctx := context.Background()

	// AWS 설정 로드
	var awsCfg aws.Config
	var err error

	if cfg.AccessKeyID != "" && cfg.SecretAccessKey != "" {
		// Access Key와 Secret Key가 제공된 경우 (개발 환경)
		awsCfg, err = config.LoadDefaultConfig(ctx,
			config.WithRegion(cfg.Region),
			config.WithCredentialsProvider(credentials.NewStaticCredentialsProvider(
				cfg.AccessKeyID,
				cfg.SecretAccessKey,
				"",
			)),
		)
	} else {
		// IAM Role 사용 (프로덕션 환경)
		awsCfg, err = config.LoadDefaultConfig(ctx,
			config.WithRegion(cfg.Region),
		)
	}

	if err != nil {
		return fmt.Errorf("failed to load AWS config: %w", err)
	}

	// S3 클라이언트 생성
	s3Client := s3.NewFromConfig(awsCfg, func(o *s3.Options) {
		// Custom endpoint 설정 (MinIO 등 사용 시)
		if cfg.Endpoint != "" {
			o.BaseEndpoint = aws.String(cfg.Endpoint)
			o.UsePathStyle = true
		}
	})

	S3 = &S3Client{
		client:     s3Client,
		bucketName: cfg.BucketName,
		region:     cfg.Region,
	}

	fmt.Println("✅ S3 client initialized successfully")
	return nil
}

// UploadFile S3에 파일 업로드
func (s *S3Client) UploadFile(ctx context.Context, file multipart.File, fileHeader *multipart.FileHeader, folder string) (string, error) {
	// 파일 확장자 추출
	ext := filepath.Ext(fileHeader.Filename)
	if ext == "" {
		ext = ".jpg" // 기본값
	}

	// 고유한 파일명 생성 (UUID + timestamp)
	filename := fmt.Sprintf("%s_%d%s", uuid.New().String(), time.Now().Unix(), ext)

	// S3 키 생성 (folder/filename)
	key := fmt.Sprintf("%s/%s", folder, filename)

	// Content-Type 결정
	contentType := fileHeader.Header.Get("Content-Type")
	if contentType == "" {
		contentType = getContentType(ext)
	}

	// 파일 읽기
	fileBytes, err := io.ReadAll(file)
	if err != nil {
		return "", fmt.Errorf("failed to read file: %w", err)
	}

	fmt.Printf("📤 S3 업로드 시작: bucket=%s, key=%s, size=%d bytes, contentType=%s\n",
		s.bucketName, key, len(fileBytes), contentType)

	// S3에 업로드 (바이너리 데이터를 올바르게 처리)
	_, err = s.client.PutObject(ctx, &s3.PutObjectInput{
		Bucket:        aws.String(s.bucketName),
		Key:           aws.String(key),
		Body:          bytes.NewReader(fileBytes),
		ContentType:   aws.String(contentType),
		ContentLength: aws.Int64(int64(len(fileBytes))),
	})

	if err != nil {
		fmt.Printf("❌ S3 업로드 실패: %v\n", err)
		return "", fmt.Errorf("failed to upload to S3: %w", err)
	}

	// URL 생성 (CloudFront 사용 시 CloudFront URL로 변경 가능)
	url := fmt.Sprintf("https://%s.s3.%s.amazonaws.com/%s", s.bucketName, s.region, key)

	fmt.Printf("✅ S3 업로드 성공: %s\n", url)

	return url, nil
}

// DeleteFile S3에서 파일 삭제
func (s *S3Client) DeleteFile(ctx context.Context, fileURL string) error {
	// URL에서 key 추출
	key := extractKeyFromURL(fileURL)
	if key == "" {
		return fmt.Errorf("invalid file URL: %s", fileURL)
	}

	_, err := s.client.DeleteObject(ctx, &s3.DeleteObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(key),
	})

	if err != nil {
		return fmt.Errorf("failed to delete from S3: %w", err)
	}

	return nil
}

// extractKeyFromURL URL에서 S3 키 추출
func extractKeyFromURL(fileURL string) string {
	// https://bucket-name.s3.region.amazonaws.com/folder/filename 형식에서 folder/filename 추출
	parts := strings.Split(fileURL, ".amazonaws.com/")
	if len(parts) != 2 {
		return ""
	}
	return parts[1]
}

// getContentType 파일 확장자로 Content-Type 결정
func getContentType(ext string) string {
	contentTypes := map[string]string{
		".jpg":  "image/jpeg",
		".jpeg": "image/jpeg",
		".png":  "image/png",
		".gif":  "image/gif",
		".webp": "image/webp",
		".svg":  "image/svg+xml",
	}

	if contentType, ok := contentTypes[strings.ToLower(ext)]; ok {
		return contentType
	}

	return "application/octet-stream"
}
