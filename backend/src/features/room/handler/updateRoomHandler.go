package handler

import (
	"context"
	"main/features/room/model/request"
	"net/http"

	"github.com/labstack/echo/v4"
	"github.com/go-playground/validator/v10"
)

type UpdateRoomHandler struct {
	useCase UpdateRoomUseCaseInterface
}

type UpdateRoomUseCaseInterface interface {
	Execute(ctx context.Context, requesterUserID int64, req *request.UpdateRoomRequest) error
}

func NewUpdateRoomHandler(useCase UpdateRoomUseCaseInterface) *UpdateRoomHandler {
	return &UpdateRoomHandler{useCase: useCase}
}

func (h *UpdateRoomHandler) UpdateRoom(c echo.Context) error {
	// 토큰에서 user_id 추출
	userID, ok := c.Get("user_id").(int64)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{
			"error": "Unauthorized",
		})
	}

	// 요청 파싱
	var req request.UpdateRoomRequest
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "Invalid request format",
		})
	}

	// 유효성 검증
	validate := validator.New()
	if err := validate.Struct(req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "Validation failed: " + err.Error(),
		})
	}

	// UseCase 실행
	err := h.useCase.Execute(c.Request().Context(), userID, &req)
	if err != nil {
		switch err.Error() {
		case "Only room owner can update room settings":
			return c.JSON(http.StatusForbidden, map[string]string{
				"error": err.Error(),
			})
		default:
			return c.JSON(http.StatusInternalServerError, map[string]string{
				"error": "Failed to update room settings",
			})
		}
	}

	return c.JSON(http.StatusOK, map[string]string{
		"message": "Room settings updated successfully",
	})
}
