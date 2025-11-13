package handler

import (
	"encoding/json"
	"fmt"
	"main/common"
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/request"
	_middleware "main/middleware"
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
	c.PUT("/v0.1/memo/:id", handler.UpdateMemo, _middleware.TokenChecker)
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

	// JWT에서 userID 추출
	userID, ok := c.Get("uID").(uint)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{
			"error": "user authentication required",
		})
	}

	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid memo id"})
	}

	var req request.ReqUpdateMemo

	// Content-Type에 따라 다르게 처리
	contentType := c.Request().Header.Get("Content-Type")

	// JSON Body로 받는 경우 (기본)
	if contentType == "application/json" || contentType == "" {
		if err := c.Bind(&req); err != nil {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request body"})
		}
		fmt.Printf("[UpdateMemo] Parsed request - Title: %s, Rating: %d, CategoryIds: %v\n", req.Title, req.Rating, req.CategoryIds)
	} else {
		// Multipart Form 데이터로 받는 경우 (이미지 직접 업로드 시)
		req = request.ReqUpdateMemo{
			Title:   c.FormValue("title"),
			Content: c.FormValue("content"),
		}

		// Rating 파싱 (Float로 받음, 소수점 지원)
		if ratingStr := c.FormValue("rating"); ratingStr != "" {
			if rating, err := strconv.ParseFloat(ratingStr, 32); err == nil {
				req.Rating = float32(rating)
			}
		}

		// IsPinned 파싱 (optional)
		if pinnedStr := c.FormValue("is_pinned"); pinnedStr != "" {
			if pinned, err := strconv.ParseBool(pinnedStr); err == nil {
				req.IsPinned = pinned
			}
		}

		// IsWishlist 파싱 (optional)
		if wishlistStr := c.FormValue("is_wishlist"); wishlistStr != "" {
			if wishlist, err := strconv.ParseBool(wishlistStr); err == nil {
				req.IsWishlist = wishlist
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

		// BusinessName 파싱 (optional)
		if bizName := c.FormValue("business_name"); bizName != "" {
			req.BusinessName = &bizName
		}

		// BusinessPhone 파싱 (optional)
		if bizPhone := c.FormValue("business_phone"); bizPhone != "" {
			req.BusinessPhone = &bizPhone
		}

		// BusinessAddress 파싱 (optional)
		if bizAddr := c.FormValue("business_address"); bizAddr != "" {
			req.BusinessAddress = &bizAddr
		}

		// CategoryIds 파싱 (optional)
		if categoryIdsStr := c.FormValue("category_ids"); categoryIdsStr != "" {
			var categoryIds []int
			if err := json.Unmarshal([]byte(categoryIdsStr), &categoryIds); err == nil {
				req.CategoryIds = categoryIds
			}
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
	}

	memo, err := h.UseCase.UpdateMemo(ctx, uint(id), userID, req)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, memo)
}
