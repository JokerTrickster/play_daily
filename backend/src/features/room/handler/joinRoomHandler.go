package handler

import (
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"net/http"

	"github.com/labstack/echo/v4"
)

type JoinRoomHandler struct {
	UseCase _interface.IJoinRoomUseCase
}

func NewJoinRoomHandler(useCase _interface.IJoinRoomUseCase) _interface.IJoinRoomHandler {
	return &JoinRoomHandler{
		UseCase: useCase,
	}
}

// JoinRoom handles the room join request
// @Router /v0.1/rooms/join [post]
// @Summary Join a room by room code
// @Description Join an existing room by providing room code and optional password
// @Accept json
// @Produce json
// @Param request body request.ReqJoinRoom true "Join room request"
// @Success 200 {object} response.ResRoom
// @Failure 400 {object} map[string]interface{}
// @Failure 401 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags room
// @Security Bearer
func (h *JoinRoomHandler) JoinRoom(c echo.Context) error {
	ctx := c.Request().Context()

	// Get user ID from context (set by TokenChecker middleware)
	userID, ok := c.Get("uID").(uint)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{"error": "unauthorized"})
	}

	// Bind request
	var req request.ReqJoinRoom
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request body"})
	}

	// Manual validation
	if req.RoomCode == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "room_code is required"})
	}

	// Execute use case
	res, err := h.UseCase.JoinRoom(ctx, userID, &req)
	if err != nil {
		if err.Error() == "room not found" {
			return c.JSON(http.StatusNotFound, map[string]string{"error": "room not found"})
		}
		if err.Error() == "invalid password" {
			return c.JSON(http.StatusUnauthorized, map[string]string{"error": "invalid password"})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "internal server error"})
	}

	return c.JSON(http.StatusOK, res)
}
