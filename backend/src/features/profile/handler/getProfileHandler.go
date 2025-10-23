package handler

import (
	_interface "main/features/profile/model/interface"
	"net/http"

	"github.com/labstack/echo/v4"
)

type GetProfileHandler struct {
	UseCase _interface.IGetProfileUseCase
}

func NewGetProfileHandler(c *echo.Echo, useCase _interface.IGetProfileUseCase) _interface.IGetProfileHandler {
	handler := &GetProfileHandler{
		UseCase: useCase,
	}
	c.GET("/v0.1/profile", handler.GetProfile)
	return handler
}

// GetProfile 프로필 조회 API
// @Router /v0.1/profile [get]
// @Summary 프로필 조회 API
// @Description 현재 사용자의 프로필 정보를 조회합니다
// @Produce json
// @Success 200 {object} response.ResProfile
// @Failure 401 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags profile
func (h *GetProfileHandler) GetProfile(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	profile, err := h.UseCase.GetProfile(ctx, userID)
	if err != nil {
		return c.JSON(http.StatusNotFound, map[string]string{"error": "user not found"})
	}

	return c.JSON(http.StatusOK, profile)
}
