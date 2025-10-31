package handler

import (
	_interface "main/features/roomlike/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type LikeRoomHandler struct {
	UseCase _interface.ILikeRoomUseCase
}

func NewLikeRoomHandler(c *echo.Echo, useCase _interface.ILikeRoomUseCase) _interface.ILikeRoomHandler {
	handler := &LikeRoomHandler{
		UseCase: useCase,
	}
	c.POST("/v0.1/room/:room_id/like", handler.Like)
	return handler
}

// Like 방 좋아요 추가 API
// @Router /v0.1/room/{room_id}/like [post]
// @Summary 방 좋아요 추가 API
// @Description 방에 좋아요를 추가합니다
// @Accept json
// @Produce json
// @Param room_id path integer true "방 ID"
// @Success 201 {object} map[string]interface{}
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags roomlike
func (h *LikeRoomHandler) Like(c echo.Context) error {
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
		if err.Error() == "already liked this room" {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": err.Error()})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusCreated, map[string]string{"message": "room liked successfully"})
}
