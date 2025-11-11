package handler

import (
	_interface "main/features/room/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type RoomDiscoveryHandler struct {
	UseCase _interface.IRoomDiscoveryUseCase
}

func NewRoomDiscoveryHandler(c *echo.Echo, useCase _interface.IRoomDiscoveryUseCase) _interface.IRoomDiscoveryHandler {
	handler := &RoomDiscoveryHandler{
		UseCase: useCase,
	}
	// Register routes - no authentication required for discovery
	c.GET("/v0.1/rooms/popular", handler.GetPopularRooms)
	c.GET("/v0.1/rooms/recent", handler.GetRecentRooms)
	return handler
}

// GetPopularRooms retrieves popular public rooms
// @Router /v0.1/rooms/popular [get]
// @Summary Get popular rooms API
// @Description Retrieves public rooms sorted by likes count (descending) and creation date
// @Produce json
// @Param page query int false "Page number (default: 1)"
// @Param limit query int false "Items per page (default: 20, max: 100)"
// @Success 200 {object} response.ResRoomList
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags room
func (h *RoomDiscoveryHandler) GetPopularRooms(c echo.Context) error {
	ctx := c.Request().Context()

	// Parse and validate page parameter (default: 1)
	page := 1
	pageStr := c.QueryParam("page")
	if pageStr != "" {
		parsedPage, err := strconv.Atoi(pageStr)
		if err != nil || parsedPage < 1 {
			return c.JSON(http.StatusBadRequest, map[string]string{
				"error": "invalid page number (must be >= 1)",
			})
		}
		page = parsedPage
	}

	// Parse and validate limit parameter (default: 20, max: 100)
	limit := 20
	limitStr := c.QueryParam("limit")
	if limitStr != "" {
		parsedLimit, err := strconv.Atoi(limitStr)
		if err != nil || parsedLimit < 1 {
			return c.JSON(http.StatusBadRequest, map[string]string{
				"error": "invalid limit (must be >= 1)",
			})
		}
		if parsedLimit > 100 {
			parsedLimit = 100
		}
		limit = parsedLimit
	}

	// Get popular rooms
	roomList, err := h.UseCase.GetPopularRooms(ctx, page, limit)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusOK, roomList)
}

// GetRecentRooms retrieves recently created public rooms
// @Router /v0.1/rooms/recent [get]
// @Summary Get recent rooms API
// @Description Retrieves public rooms sorted by creation date (descending) and likes count
// @Produce json
// @Param page query int false "Page number (default: 1)"
// @Param limit query int false "Items per page (default: 20, max: 100)"
// @Success 200 {object} response.ResRoomList
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags room
func (h *RoomDiscoveryHandler) GetRecentRooms(c echo.Context) error {
	ctx := c.Request().Context()

	// Parse and validate page parameter (default: 1)
	page := 1
	pageStr := c.QueryParam("page")
	if pageStr != "" {
		parsedPage, err := strconv.Atoi(pageStr)
		if err != nil || parsedPage < 1 {
			return c.JSON(http.StatusBadRequest, map[string]string{
				"error": "invalid page number (must be >= 1)",
			})
		}
		page = parsedPage
	}

	// Parse and validate limit parameter (default: 20, max: 100)
	limit := 20
	limitStr := c.QueryParam("limit")
	if limitStr != "" {
		parsedLimit, err := strconv.Atoi(limitStr)
		if err != nil || parsedLimit < 1 {
			return c.JSON(http.StatusBadRequest, map[string]string{
				"error": "invalid limit (must be >= 1)",
			})
		}
		if parsedLimit > 100 {
			parsedLimit = 100
		}
		limit = parsedLimit
	}

	// Get recent rooms
	roomList, err := h.UseCase.GetRecentRooms(ctx, page, limit)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusOK, roomList)
}
