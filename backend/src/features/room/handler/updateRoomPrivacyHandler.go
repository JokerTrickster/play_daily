package handler

import (
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type UpdateRoomPrivacyHandler struct {
	UpdateRoomPrivacyUseCase _interface.IUpdateRoomPrivacyUseCase
}

func NewUpdateRoomPrivacyHandler(useCase _interface.IUpdateRoomPrivacyUseCase) _interface.IUpdateRoomPrivacyHandler {
	return &UpdateRoomPrivacyHandler{
		UpdateRoomPrivacyUseCase: useCase,
	}
}

// UpdateRoomPrivacy handles the room privacy toggle request
// @Router /v0.1/rooms/:room_id/privacy [put]
// @Summary Toggle room privacy settings
// @Description Allow room owners to toggle between public and private modes
// @Accept json
// @Produce json
// @Param room_id path uint true "Room ID"
// @Param request body request.ReqUpdateRoomPrivacy true "Privacy settings"
// @Success 200 {object} response.ResUpdateRoomPrivacy
// @Failure 400 {object} map[string]interface{}
// @Failure 403 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags room
// @Security Bearer
func (h *UpdateRoomPrivacyHandler) UpdateRoomPrivacy(c echo.Context) error {
	ctx := c.Request().Context()

	// Get user ID from context (set by TokenChecker middleware)
	userID, ok := c.Get("uID").(uint)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{"error": "unauthorized"})
	}

	// Get room ID from path parameter
	roomIDStr := c.Param("room_id")
	roomID, err := strconv.ParseUint(roomIDStr, 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid room_id"})
	}

	// Bind request
	var req request.ReqUpdateRoomPrivacy
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request body"})
	}

	// Execute use case
	res, err := h.UpdateRoomPrivacyUseCase.UpdateRoomPrivacy(ctx, uint(roomID), userID, &req)
	if err != nil {
		if err.Error() == "room not found" {
			return c.JSON(http.StatusNotFound, map[string]string{"error": "room not found"})
		}
		if err.Error() == "not room owner" {
			return c.JSON(http.StatusForbidden, map[string]string{"error": "only room owner can modify privacy settings"})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "internal server error"})
	}

	return c.JSON(http.StatusOK, res)
}
