package handler

import (
	"context"
	"net/http"
	"strconv"

	"main/features/room/model/response"

	"github.com/labstack/echo/v4"
)

type GetRoomDetailHandler struct {
	useCase GetRoomDetailUseCaseInterface
}

type GetRoomDetailUseCaseInterface interface {
	Execute(ctx context.Context, roomID uint) (*response.ResRoomDetail, error)
}

func NewGetRoomDetailHandler(useCase GetRoomDetailUseCaseInterface) *GetRoomDetailHandler {
	return &GetRoomDetailHandler{useCase: useCase}
}

func (h *GetRoomDetailHandler) GetRoomDetail(c echo.Context) error {
	ctx := c.Request().Context()

	// Get room ID from path parameter
	roomIDStr := c.Param("id")
	roomID, err := strconv.ParseUint(roomIDStr, 10, 64)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]interface{}{
			"error": "Invalid room ID",
		})
	}

	// Execute use case
	roomDetail, err := h.useCase.Execute(ctx, uint(roomID))
	if err != nil {
		if err.Error() == "room not found" {
			return c.JSON(http.StatusNotFound, map[string]interface{}{
				"error": "Room not found",
			})
		}
		return c.JSON(http.StatusInternalServerError, map[string]interface{}{
			"error": "Failed to get room detail",
		})
	}

	return c.JSON(http.StatusOK, roomDetail)
}
