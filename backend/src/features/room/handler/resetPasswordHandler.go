package handler

import (
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"net/http"
	"strconv"
	"strings"

	"github.com/labstack/echo/v4"
)

type ResetPasswordHandler struct {
	UseCase _interface.IResetPasswordUseCase
}

func NewResetPasswordHandler(useCase _interface.IResetPasswordUseCase) _interface.IResetPasswordHandler {
	return &ResetPasswordHandler{
		UseCase: useCase,
	}
}

// ResetPassword handles the room password reset request
// @Router /v0.1/rooms/{id}/reset-password [post]
// @Summary Reset room password
// @Description Reset room password with secure auto-generated password. Only room owner can reset. Limited to 3 resets per 24 hours.
// @Accept json
// @Produce json
// @Param id path int true "Room ID"
// @Success 200 {object} response.ResResetPassword
// @Failure 400 {object} map[string]interface{}
// @Failure 401 {object} map[string]interface{}
// @Failure 403 {object} map[string]interface{}
// @Failure 429 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags room
// @Security Bearer
func (h *ResetPasswordHandler) ResetPassword(c echo.Context) error {
	ctx := c.Request().Context()

	// Get user ID from context (set by TokenChecker middleware)
	userID, ok := c.Get("uID").(uint)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{"error": "unauthorized"})
	}

	// Parse room ID from path parameter
	roomIDStr := c.Param("id")
	roomID, err := strconv.ParseUint(roomIDStr, 10, 32)
	if err != nil || roomID == 0 {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid room_id"})
	}

	// Create request object
	req := &request.ReqResetPassword{
		RoomID: uint(roomID),
	}

	// Extract IP address
	ipAddress := getClientIP(c)

	// Extract User-Agent
	userAgent := c.Request().Header.Get("User-Agent")
	if userAgent == "" {
		userAgent = "Unknown"
	}

	// Execute use case
	res, err := h.UseCase.ResetPassword(ctx, userID, req, &ipAddress, &userAgent)
	if err != nil {
		if err.Error() == "only room owner can reset password" {
			return c.JSON(http.StatusForbidden, map[string]string{"error": "only room owner can reset password"})
		}
		if strings.Contains(err.Error(), "rate limit exceeded") {
			// Return 429 Too Many Requests with Retry-After header (24 hours)
			c.Response().Header().Set("Retry-After", "86400") // 24 hours in seconds
			return c.JSON(http.StatusTooManyRequests, map[string]string{
				"error": err.Error(),
			})
		}
		if err.Error() == "room not found" {
			return c.JSON(http.StatusNotFound, map[string]string{"error": "room not found"})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "internal server error"})
	}

	return c.JSON(http.StatusOK, res)
}

// getClientIP extracts the real client IP address from various headers
func getClientIP(c echo.Context) string {
	// Check X-Forwarded-For header (commonly used by proxies)
	xff := c.Request().Header.Get("X-Forwarded-For")
	if xff != "" {
		// X-Forwarded-For can contain multiple IPs, take the first one
		ips := strings.Split(xff, ",")
		if len(ips) > 0 {
			return strings.TrimSpace(ips[0])
		}
	}

	// Check X-Real-IP header (used by nginx and others)
	xri := c.Request().Header.Get("X-Real-IP")
	if xri != "" {
		return strings.TrimSpace(xri)
	}

	// Fall back to RemoteAddr
	ip := c.Request().RemoteAddr
	// Remove port if present
	if idx := strings.LastIndex(ip, ":"); idx != -1 {
		ip = ip[:idx]
	}

	return ip
}
