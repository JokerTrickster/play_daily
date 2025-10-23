package handler

import (
	_interface "main/features/memo/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type GetMemoHandler struct {
	UseCase _interface.IGetMemoUseCase
}

func NewGetMemoHandler(c *echo.Echo, useCase _interface.IGetMemoUseCase) _interface.IGetMemoHandler {
	handler := &GetMemoHandler{
		UseCase: useCase,
	}
	c.GET("/v0.1/memo/:id", handler.GetMemo)
	c.GET("/v0.1/memo", handler.GetMemoList)
	return handler
}

// GetMemo 메모 조회 API
// @Router /v0.1/memo/{id} [get]
// @Summary 메모 조회 API
// @Description 특정 메모를 조회합니다
// @Produce json
// @Param id path int true "메모 ID"
// @Success 200 {object} response.ResMemo
// @Failure 400 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memo
func (h *GetMemoHandler) GetMemo(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid memo id"})
	}

	memo, err := h.UseCase.GetMemo(ctx, uint(id), userID)
	if err != nil {
		return c.JSON(http.StatusNotFound, map[string]string{"error": "memo not found"})
	}

	return c.JSON(http.StatusOK, memo)
}

// GetMemoList 메모 목록 조회 API
// @Router /v0.1/memo [get]
// @Summary 메모 목록 조회 API
// @Description 사용자의 모든 메모를 조회합니다 (is_wishlist, room_id 필터 지원)
// @Produce json
// @Param is_wishlist query bool false "Wishlist filter"
// @Param room_id query int false "Room ID filter"
// @Success 200 {object} response.ResMemoList
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memo
func (h *GetMemoHandler) GetMemoList(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	// is_wishlist 파싱 및 검증
	var isWishlist *bool
	isWishlistStr := c.QueryParam("is_wishlist")
	if isWishlistStr != "" {
		parsedIsWishlist, err := strconv.ParseBool(isWishlistStr)
		if err != nil {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid is_wishlist format (expected: true or false)"})
		}
		isWishlist = &parsedIsWishlist
	}

	// room_id 파싱 및 검증
	var roomID *uint
	roomIDStr := c.QueryParam("room_id")
	if roomIDStr != "" {
		parsedRoomID, err := strconv.ParseUint(roomIDStr, 10, 32)
		if err != nil {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid room_id format"})
		}
		roomIDUint := uint(parsedRoomID)
		roomID = &roomIDUint
	}

	// 메모 목록 조회
	memoList, err := h.UseCase.GetMemoList(ctx, userID, roomID, isWishlist)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, memoList)
}
