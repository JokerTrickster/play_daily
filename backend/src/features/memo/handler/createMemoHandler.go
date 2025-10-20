package handler

import (
	"fmt"
	"main/common"
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/request"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type CreateMemoHandler struct {
	UseCase _interface.ICreateMemoUseCase
}

func NewCreateMemoHandler(c *echo.Echo, useCase _interface.ICreateMemoUseCase) _interface.ICreateMemoHandler {
	handler := &CreateMemoHandler{
		UseCase: useCase,
	}
	c.POST("/v0.1/memo", handler.CreateMemo)
	return handler
}

// CreateMemo 메모 생성 API
// @Router /v0.1/memo [post]
// @Summary 메모 생성 API
// @Description 새로운 메모를 생성합니다 (이미지 파일 포함 가능)
// @Accept multipart/form-data
// @Produce json
// @Param title formData string true "메모 제목"
// @Param content formData string false "메모 내용"
// @Param image formData file false "이미지 파일"
// @Param rating formData integer false "평점 (0-5)"
// @Param is_pinned formData boolean false "고정 여부"
// @Param latitude formData number false "위도"
// @Param longitude formData number false "경도"
// @Param location_name formData string false "장소명"
// @Param category formData string false "카테고리"
// @Success 201 {object} response.ResMemo
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memo
func (h *CreateMemoHandler) CreateMemo(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	// Form 데이터 파싱
	req := request.ReqCreateMemo{
		Title:   c.FormValue("title"),
		Content: c.FormValue("content"),
	}

	// Rating 파싱
	if ratingStr := c.FormValue("rating"); ratingStr != "" {
		if rating, err := strconv.ParseUint(ratingStr, 10, 8); err == nil {
			req.Rating = uint8(rating)
		}
	}

	// IsPinned 파싱
	if pinnedStr := c.FormValue("is_pinned"); pinnedStr != "" {
		if pinned, err := strconv.ParseBool(pinnedStr); err == nil {
			req.IsPinned = pinned
		}
	}

	// Latitude 파싱
	if latStr := c.FormValue("latitude"); latStr != "" {
		if lat, err := strconv.ParseFloat(latStr, 64); err == nil {
			req.Latitude = &lat
		}
	}

	// Longitude 파싱
	if lngStr := c.FormValue("longitude"); lngStr != "" {
		if lng, err := strconv.ParseFloat(lngStr, 64); err == nil {
			req.Longitude = &lng
		}
	}

	// LocationName 파싱
	if locName := c.FormValue("location_name"); locName != "" {
		req.LocationName = &locName
	}

	// Category 파싱
	if category := c.FormValue("category"); category != "" {
		req.Category = &category
	}

	// 이미지 파일 검증 및 처리
	fileHeader, err := c.FormFile("image")
	if err == nil && fileHeader != nil {
		// 파일 크기 검증
		if fileHeader.Size > common.Env.MaxFileSize {
			return c.JSON(http.StatusBadRequest, map[string]string{
				"error": fmt.Sprintf("file size exceeds maximum allowed size (%d bytes)", common.Env.MaxFileSize),
			})
		}

		// 파일 열기
		file, err := fileHeader.Open()
		if err != nil {
			return c.JSON(http.StatusInternalServerError, map[string]string{
				"error": "failed to open image file",
			})
		}
		defer file.Close()

		// Request에 파일 정보 담기 (UseCase에서 S3 업로드 처리)
		req.ImageFile = file
		req.ImageHeader = fileHeader
	}

	// 제목 필수 검증
	if req.Title == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "title is required"})
	}

	memo, err := h.UseCase.CreateMemo(ctx, userID, req)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusCreated, memo)
}
