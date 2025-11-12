package handler

import (
	_interface "main/features/auth/model/interface"
	"main/features/auth/model/request"
	"net/http"
	"strings"

	"github.com/labstack/echo/v4"
)

type ReissueAuthHandler struct {
	UseCase _interface.IReissueAuthUseCase
}

func NewReissueAuthHandler(c *echo.Echo, useCase _interface.IReissueAuthUseCase) _interface.IReissueAuthHandler {
	handler := &ReissueAuthHandler{
		UseCase: useCase,
	}
	c.POST("/v0.1/auth/reissue", handler.Reissue)
	return handler
}

// Reissue 토큰 재발급 API
// @Router /v0.1/auth/reissue [post]
// @Summary 토큰 재발급 API
// @Description Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다
// @Accept json
// @Produce json
// @Param request body request.ReqReissue true "토큰 재발급 요청 데이터"
// @Success 200 {object} response.ResReissue
// @Failure 400 {object} map[string]interface{} "잘못된 요청 형식"
// @Failure 401 {object} map[string]interface{} "유효하지 않은 토큰"
// @Failure 500 {object} map[string]interface{} "서버 내부 오류"
// @Tags auth
func (h *ReissueAuthHandler) Reissue(c echo.Context) error {
	ctx := c.Request().Context()
	var req request.ReqReissue
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error":   "invalid request",
			"message": "요청 형식이 올바르지 않습니다",
		})
	}

	// Validate request fields
	if req.AccessToken == "" || req.RefreshToken == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error":   "missing required fields",
			"message": "access_token과 refresh_token은 필수입니다",
		})
	}

	res, err := h.UseCase.Reissue(ctx, req)
	if err != nil {
		// Determine error type and return appropriate status code
		errMsg := err.Error()
		if strings.Contains(errMsg, "signature") || strings.Contains(errMsg, "invalid") ||
		   strings.Contains(errMsg, "expired") || strings.Contains(errMsg, "not found") ||
		   strings.Contains(errMsg, "revoked") {
			return c.JSON(http.StatusUnauthorized, map[string]string{
				"error":   "authentication failed",
				"message": errMsg,
			})
		}
		// Internal server error
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error":   "internal server error",
			"message": "토큰 재발급 중 오류가 발생했습니다",
		})
	}

	return c.JSON(http.StatusOK, res)
}
