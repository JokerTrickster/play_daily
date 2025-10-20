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

type UpdateMemoHandler struct {
	UseCase _interface.IUpdateMemoUseCase
}

func NewUpdateMemoHandler(c *echo.Echo, useCase _interface.IUpdateMemoUseCase) _interface.IUpdateMemoHandler {
	handler := &UpdateMemoHandler{
		UseCase: useCase,
	}
	c.PUT("/v0.1/memo/:id", handler.UpdateMemo)
	return handler
}

// UpdateMemo 메모 수정 API
// @Router /v0.1/memo/{id} [put]
// @Summary 메모 수정 API
// @Description 메모를 수정합니다 (이미지 파일 포함 가능)
// @Accept multipart/form-data
// @Produce json
// @Param id path int true "메모 ID"
// @Param title formData string false "메모 제목"
// @Param content formData string false "메모 내용"
// @Param image formData file false "이미지 파일"
// @Param rating formData integer false "평점 (0-5)"
// @Param is_pinned formData boolean false "고정 여부"
// @Param latitude formData number false "위도"
// @Param longitude formData number false "경도"
// @Param location_name formData string false "장소명"
// @Param category formData string false "카테고리"
// @Success 200 {object} response.ResMemo
// @Failure 400 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memo
func (h *UpdateMemoHandler) UpdateMemo(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid memo id"})
	}

	// Form 데이터 파싱
	req := request.ReqUpdateMemo{
		Title:   c.FormValue("title"),
		Content: c.FormValue("content"),
	}

	// Rating 파싱 (optional)
	if ratingStr := c.FormValue("rating"); ratingStr != "" {
		if rating, err := strconv.ParseUint(ratingStr, 10, 8); err == nil {
			req.Rating = uint8(rating)
		}
	}

	// IsPinned 파싱 (optional)
	if pinnedStr := c.FormValue("is_pinned"); pinnedStr != "" {
		if pinned, err := strconv.ParseBool(pinnedStr); err == nil {
			req.IsPinned = pinned
		}
	}

	// Latitude 파싱 (optional)
	if latStr := c.FormValue("latitude"); latStr != "" {
		if lat, err := strconv.ParseFloat(latStr, 64); err == nil {
			req.Latitude = &lat
		}
	}

	// Longitude 파싱 (optional)
	if lngStr := c.FormValue("longitude"); lngStr != "" {
		if lng, err := strconv.ParseFloat(lngStr, 64); err == nil {
			req.Longitude = &lng
		}
	}

	// LocationName 파싱 (optional)
	if locName := c.FormValue("location_name"); locName != "" {
		req.LocationName = &locName
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

	memo, err := h.UseCase.UpdateMemo(ctx, uint(id), userID, req)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, memo)
}
