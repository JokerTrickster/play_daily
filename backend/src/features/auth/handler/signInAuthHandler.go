package handler

import (
	_interface "main/features/auth/model/interface"
	"main/features/auth/model/request"
	"net/http"

	"github.com/labstack/echo/v4"
)

type SignInAuthHandler struct {
	UseCase _interface.ISignInAuthUseCase
}

func NewSignInAuthHandler(c *echo.Echo, useCase _interface.ISignInAuthUseCase) _interface.ISignInAuthHandler {
	handler := &SignInAuthHandler{
		UseCase: useCase,
	}
	c.POST("/v0.1/auth/signin", handler.SignIn)
	return handler
}

// 로그인 api
// @Router /v0.1/auth/signin [post]
// @Summary 로그인 api
// @Description 로그인 api
// @Accept json
// @Produce json
// @Param request body request.ReqSignIn true "로그인 요청 데이터"
// @Success 200 {object} response.ResAuth
// @Failure 400 {object} map[string]interface{}
// @Failure 401 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags auth
func (h *SignInAuthHandler) SignIn(c echo.Context) error {
	ctx := c.Request().Context()
	var req request.ReqSignIn
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request"})
	}

	res, err := h.UseCase.SignIn(ctx, req)
	if err != nil {
		return c.JSON(http.StatusUnauthorized, map[string]string{"error": "authentication failed"})
	}

	return c.JSON(http.StatusOK, res)
}
