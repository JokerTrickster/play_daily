package storage

import (
	"bytes"
	"context"
	"fmt"
	"image"
	"image/jpeg"
	"image/png"
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
	"golang.org/x/image/draw"
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

	// 이미지 파일인 경우 리사이징 처리
	var processedBytes []byte
	if strings.HasPrefix(contentType, "image/") && contentType != "image/svg+xml" {
		processedBytes, err = resizeImage(fileBytes, ext, 1200, 1200)
		if err != nil {
			fmt.Printf("⚠️  이미지 리사이징 실패, 원본 사용: %v\n", err)
			processedBytes = fileBytes
		} else {
			fmt.Printf("✨ 이미지 리사이징 완료: %d bytes -> %d bytes\n", len(fileBytes), len(processedBytes))
		}
	} else {
		processedBytes = fileBytes
	}

	fmt.Printf("📤 S3 업로드 시작: bucket=%s, key=%s, size=%d bytes, contentType=%s\n",
		s.bucketName, key, len(processedBytes), contentType)

	// S3에 업로드 (바이너리 데이터를 올바르게 처리)
	_, err = s.client.PutObject(ctx, &s3.PutObjectInput{
		Bucket:        aws.String(s.bucketName),
		Key:           aws.String(key),
		Body:          bytes.NewReader(processedBytes),
		ContentType:   aws.String(contentType),
		ContentLength: aws.Int64(int64(len(processedBytes))),
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

// resizeImage 이미지를 최대 크기로 리사이징 (비율 유지)
func resizeImage(data []byte, ext string, maxWidth, maxHeight int) ([]byte, error) {
	// 이미지 디코딩
	img, format, err := image.Decode(bytes.NewReader(data))
	if err != nil {
		return nil, fmt.Errorf("failed to decode image: %w", err)
	}

	// 원본 크기
	bounds := img.Bounds()
	origWidth := bounds.Dx()
	origHeight := bounds.Dy()

	// 리사이징이 필요한지 확인
	if origWidth <= maxWidth && origHeight <= maxHeight {
		// 리사이징 불필요, 원본 반환
		return data, nil
	}

	// 새로운 크기 계산 (비율 유지)
	var newWidth, newHeight int
	ratio := float64(origWidth) / float64(origHeight)

	if origWidth > origHeight {
		// 가로가 더 긴 경우
		newWidth = maxWidth
		newHeight = int(float64(maxWidth) / ratio)
		if newHeight > maxHeight {
			newHeight = maxHeight
			newWidth = int(float64(maxHeight) * ratio)
		}
	} else {
		// 세로가 더 긴 경우
		newHeight = maxHeight
		newWidth = int(float64(maxHeight) * ratio)
		if newWidth > maxWidth {
			newWidth = maxWidth
			newHeight = int(float64(maxWidth) / ratio)
		}
	}

	// 리사이징된 이미지 생성
	dst := image.NewRGBA(image.Rect(0, 0, newWidth, newHeight))
	draw.BiLinear.Scale(dst, dst.Bounds(), img, img.Bounds(), draw.Over, nil)

	// 버퍼에 인코딩
	var buf bytes.Buffer
	switch strings.ToLower(format) {
	case "jpeg", "jpg":
		err = jpeg.Encode(&buf, dst, &jpeg.Options{Quality: 85})
	case "png":
		err = png.Encode(&buf, dst)
	default:
		// 기본적으로 JPEG로 인코딩
		err = jpeg.Encode(&buf, dst, &jpeg.Options{Quality: 85})
	}

	if err != nil {
		return nil, fmt.Errorf("failed to encode resized image: %w", err)
	}

	return buf.Bytes(), nil
}
