package handler

import (
	_interface "main/features/roomlike/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type UnlikeRoomHandler struct {
	UseCase _interface.IUnlikeRoomUseCase
}

func NewUnlikeRoomHandler(c *echo.Echo, useCase _interface.IUnlikeRoomUseCase) _interface.IUnlikeRoomHandler {
	handler := &UnlikeRoomHandler{
		UseCase: useCase,
	}
	c.DELETE("/v0.1/room/:room_id/like", handler.Unlike)
	return handler
}

// Unlike 방 좋아요 취소 API
// @Router /v0.1/room/{room_id}/like [delete]
// @Summary 방 좋아요 취소 API
// @Description 방의 좋아요를 취소합니다
// @Accept json
// @Produce json
// @Param room_id path integer true "방 ID"
// @Success 200 {object} map[string]interface{}
// @Failure 400 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags roomlike
func (h *UnlikeRoomHandler) Unlike(c echo.Context) error {
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
	err = h.UseCase.Execute(ctx, uint(roomID), userID)
	if err != nil {
		if err.Error() == "room like not found" {
			return c.JSON(http.StatusNotFound, map[string]string{"error": err.Error()})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, map[string]string{"message": "room unliked successfully"})
}
