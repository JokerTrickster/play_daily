package handler

import (
	"main/common/storage"
	"net/http"

	"github.com/labstack/echo/v4"
)

type UploadProfileImageHandler struct{}

func NewUploadProfileImageHandler(c *echo.Echo) *UploadProfileImageHandler {
	handler := &UploadProfileImageHandler{}
	c.POST("/v0.1/profile/image", handler.UploadProfileImage)
	return handler
}

// UploadProfileImage 프로필 이미지 업로드 API
// @Router /v0.1/profile/image [post]
// @Summary 프로필 이미지 업로드
// @Description 프로필 이미지를 S3에 업로드하고 URL 반환
// @Tags Profile
// @Accept multipart/form-data
// @Produce json
// @Param image formData file true "프로필 이미지 파일"
// @Success 200 {object} map[string]string "업로드된 이미지 URL"
// @Failure 400 {object} map[string]string "잘못된 요청"
// @Failure 500 {object} map[string]string "서버 오류"
func (h *UploadProfileImageHandler) UploadProfileImage(c echo.Context) error {
	ctx := c.Request().Context()

	// 이미지 파일 검증 및 처리
	fileHeader, err := c.FormFile("image")
	if err != nil || fileHeader == nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "image file is required",
		})
	}

	// 파일 크기 검증 (최대 10MB)
	const maxFileSize = 10 << 20 // 10 MB
	if fileHeader.Size > maxFileSize {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "image file size exceeds 10MB limit",
		})
	}

	// 파일 열기
	file, err := fileHeader.Open()
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "failed to read image file",
		})
	}
	defer file.Close()

	// S3에 이미지 업로드 (profile 폴더에 저장)
	uploadedURL, err := storage.S3.UploadFile(ctx, file, fileHeader, "image/profile")
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": "failed to upload image to S3",
		})
	}

	return c.JSON(http.StatusOK, map[string]string{
		"image_url": uploadedURL,
	})
}
