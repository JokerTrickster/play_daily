package handler

import (
	"context"
	"main/features/room/model/request"
	"net/http"

	"github.com/go-playground/validator/v10"
	"github.com/labstack/echo/v4"
	"gorm.io/gorm"
)

type UpdatePermissionHandler struct {
	useCase UpdatePermissionUseCaseInterface
}

type UpdatePermissionUseCaseInterface interface {
	Execute(ctx context.Context, requesterUserID int64, req *request.UpdatePermissionRequest) error
}

func NewUpdatePermissionHandler(useCase UpdatePermissionUseCaseInterface) *UpdatePermissionHandler {
	return &UpdatePermissionHandler{useCase: useCase}
}

func (h *UpdatePermissionHandler) UpdatePermission(c echo.Context) error {
	// 토큰에서 user_id 추출
	userID, ok := c.Get("user_id").(int64)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{
			"error": "Unauthorized",
		})
	}

	// 요청 파싱
	var req request.UpdatePermissionRequest
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
		case "Only room owner can update permissions":
			return c.JSON(http.StatusForbidden, map[string]string{
				"error": err.Error(),
			})
		case "Cannot modify your own permission":
			return c.JSON(http.StatusBadRequest, map[string]string{
				"error": err.Error(),
			})
		case gorm.ErrRecordNotFound.Error():
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Room member not found",
			})
		default:
			return c.JSON(http.StatusInternalServerError, map[string]string{
				"error": "Failed to update permission",
			})
		}
	}

	return c.JSON(http.StatusOK, map[string]string{
		"message": "Permission updated successfully",
	})
}
