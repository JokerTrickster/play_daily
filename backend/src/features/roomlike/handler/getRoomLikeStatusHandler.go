package handler

import (
	_interface "main/features/roomlike/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type GetRoomLikeStatusHandler struct {
	UseCase _interface.IGetRoomLikeStatusUseCase
}

func NewGetRoomLikeStatusHandler(c *echo.Echo, useCase _interface.IGetRoomLikeStatusUseCase) _interface.IGetRoomLikeStatusHandler {
	handler := &GetRoomLikeStatusHandler{
		UseCase: useCase,
	}
	c.GET("/v0.1/room/:room_id/like/status", handler.GetStatus)
	return handler
}

// GetStatus 방 좋아요 상태 조회 API
// @Router /v0.1/room/{room_id}/like/status [get]
// @Summary 방 좋아요 상태 조회 API
// @Description 방의 좋아요 상태와 총 좋아요 개수를 조회합니다
// @Accept json
// @Produce json
// @Param room_id path integer true "방 ID"
// @Success 200 {object} response.ResRoomLikeStatus
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags roomlike
func (h *GetRoomLikeStatusHandler) GetStatus(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	// Path parameter에서 room_id 추출
	roomIDStr := c.Param("room_id")
	roomID, err := strconv.ParseUint(roomIDStr, 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid room_id"})
	}

	// UseCase 실행
	status, err := h.UseCase.Execute(ctx, uint(roomID), userID)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, status)
}
