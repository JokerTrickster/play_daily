package handler

import (
	"encoding/json"
	"fmt"
	"main/common"
	"main/common/db/mysql"
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/request"
	_middleware "main/middleware"
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
	c.POST("/v0.1/memo", handler.CreateMemo, _middleware.TokenChecker)
	return handler
}

// CreateMemo 메모 생성 API
// @Router /v0.1/memo [post]
// @Summary 메모 생성 API
// @Description 새로운 메모를 생성합니다 (이미지 파일 포함 가능)
// @Accept multipart/form-data
// @Produce json
// @Param title formData string true "메모 제목"
// @Param content formData string false "메모 내용 (Deprecated)"
// @Param creation_mode formData string true "생성 방식 (map 또는 list)"
// @Param category_ids formData string true "카테고리 ID 배열 (JSON 배열 형식)"
// @Param image formData file false "이미지 파일"
// @Param rating formData integer false "평점 (0-5)"
// @Param is_pinned formData boolean false "고정 여부"
// @Param latitude formData number false "위도"
// @Param longitude formData number false "경도"
// @Param location_name formData string false "장소명"
// @Param category formData string false "카테고리 (Deprecated)"
// @Success 201 {object} response.ResMemo
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memo
func (h *CreateMemoHandler) CreateMemo(c echo.Context) error {
	ctx := c.Request().Context()

	// JWT에서 userID 추출
	userID, ok := c.Get("uID").(uint)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{
			"error": "user authentication required",
		})
	}

	// 요청에서 room_id를 받거나 기본값 사용
	roomIDStr := c.FormValue("room_id")
	var roomID uint
	if roomIDStr != "" {
		if parsed, err := strconv.ParseUint(roomIDStr, 10, 32); err == nil {
			roomID = uint(parsed)
		} else {
			return c.JSON(http.StatusBadRequest, map[string]string{
				"error": "invalid room_id format",
			})
		}
	} else {
		// room_id가 제공되지 않으면 사용자의 기본 room 사용
		// 이를 위해서는 사용자 정보를 조회해야 함
		roomID = uint(1) // 임시로 1을 사용, 추후 사용자 정보에서 가져오도록 개선 필요
	}

	// Form 데이터 파싱
	req := request.ReqCreateMemo{
		RoomID: roomID,
		Title:  c.FormValue("title"),
		// Content field removed - deprecated, using categories instead
	}

	// Rating 파싱 (Float로 받음, 소수점 지원)
	if ratingStr := c.FormValue("rating"); ratingStr != "" {
		if rating, err := strconv.ParseFloat(ratingStr, 32); err == nil {
			req.Rating = float32(rating)
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

	// Category field removed - deprecated, using category_ids instead

	// IsWishlist 파싱
	if wishlistStr := c.FormValue("is_wishlist"); wishlistStr != "" {
		if wishlist, err := strconv.ParseBool(wishlistStr); err == nil {
			req.IsWishlist = wishlist
		}
	}

	// BusinessName 파싱
	if businessName := c.FormValue("business_name"); businessName != "" {
		req.BusinessName = &businessName
	}

	// BusinessPhone 파싱
	if businessPhone := c.FormValue("business_phone"); businessPhone != "" {
		req.BusinessPhone = &businessPhone
	}

	// BusinessAddress 파싱
	if businessAddress := c.FormValue("business_address"); businessAddress != "" {
		req.BusinessAddress = &businessAddress
	}

	// NaverPlaceURL 파싱
	if naverPlaceURL := c.FormValue("naver_place_url"); naverPlaceURL != "" {
		req.NaverPlaceURL = &naverPlaceURL
	}

	// CreationMode 파싱 (NEW)
	req.CreationMode = c.FormValue("creation_mode")
	if req.CreationMode == "" {
		req.CreationMode = "list" // 기본값 설정
	}

	// CategoryIDs 파싱 (NEW) - JSON 배열 형식으로 받음
	categoryIDsStr := c.FormValue("category_ids")
	if categoryIDsStr != "" {
		var categoryIDs []uint
		if err := json.Unmarshal([]byte(categoryIDsStr), &categoryIDs); err != nil {
			return c.JSON(http.StatusBadRequest, map[string]string{
				"error": "invalid category_ids format (expected JSON array)",
			})
		}
		req.CategoryIDs = categoryIDs
	}

	// 제목 필수 검증 (빠른 실패를 위해 먼저 체크)
	if req.Title == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "title is required"})
	}

	// 요청 전체 검증 (카테고리 검증 포함) - 파일 처리 전에 수행
	if err := req.ValidateCreateMemo(mysql.GormMysqlDB); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": err.Error()})
	}

	// 이미지 파일 검증 및 처리 (validation 통과 후에만 파일 처리)
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
		defer file.Close() // defer를 Open() 직후에 위치시켜 모든 경로에서 파일이 닫히도록 함

		// Request에 파일 정보 담기 (UseCase에서 S3 업로드 처리)
		req.ImageFile = file
		req.ImageHeader = fileHeader
	}

	memo, err := h.UseCase.CreateMemo(ctx, userID, req)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusCreated, memo)
}
