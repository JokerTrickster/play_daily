package handler

import (
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type GetPublicRoomsHandler struct {
	GetPublicRoomsUseCase _interface.IGetPublicRoomsUseCase
}

func NewGetPublicRoomsHandler(useCase _interface.IGetPublicRoomsUseCase) _interface.IGetPublicRoomsHandler {
	return &GetPublicRoomsHandler{
		GetPublicRoomsUseCase: useCase,
	}
}

// GetPublicRooms handles the public room discovery request
// @Router /v0.1/rooms/public [get]
// @Summary Get public rooms list
// @Description Retrieve paginated list of public rooms sorted by popularity
// @Accept json
// @Produce json
// @Param page query int false "Page number (default: 1, min: 1)"
// @Param limit query int false "Items per page (default: 10, min: 1, max: 50)"
// @Success 200 {object} response.ResGetPublicRooms
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags room
func (h *GetPublicRoomsHandler) GetPublicRooms(c echo.Context) error {
	ctx := c.Request().Context()

	// Parse query parameters with defaults
	req := request.ReqGetPublicRooms{
		Page:  1,
		Limit: 10,
	}

	// Parse page parameter
	if pageStr := c.QueryParam("page"); pageStr != "" {
		page, err := strconv.Atoi(pageStr)
		if err != nil || page < 1 {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid page parameter"})
		}
		req.Page = page
	}

	// Parse limit parameter
	if limitStr := c.QueryParam("limit"); limitStr != "" {
		limit, err := strconv.Atoi(limitStr)
		if err != nil || limit < 1 || limit > 50 {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid limit parameter (min: 1, max: 50)"})
		}
		req.Limit = limit
	}

	// Execute use case
	res, err := h.GetPublicRoomsUseCase.GetPublicRooms(ctx, &req)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "failed to fetch public rooms"})
	}

	return c.JSON(http.StatusOK, res)
}
