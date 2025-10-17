package handler

import (
	_interface "main/features/auth/model/interface"
	"main/features/auth/model/request"
	"net/http"

	"github.com/labstack/echo/v4"
)

type SignUpAuthHandler struct {
	UseCase _interface.ISignUpAuthUseCase
}

func NewSignUpAuthHandler(c *echo.Echo, useCase _interface.ISignUpAuthUseCase) _interface.ISignUpAuthHandler {
	handler := &SignUpAuthHandler{
		UseCase: useCase,
	}
	c.POST("/v0.1/auth/signup", handler.SignUp)
	return handler
}

// 회원가입 api
// @Router /v0.1/auth/signup [post]
// @Summary 회원가입 api
// @Description 회원가입 api
// @Accept json
// @Produce json
// @Param request body request.ReqSignUp true "회원가입 요청 데이터"
// @Success 200 {object} response.ResLiveLearning
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags auth
func (h *SignUpAuthHandler) SignUp(c echo.Context) error {
	ctx := c.Request().Context()
	var req request.ReqSignUp
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request"})
	}

	err := h.UseCase.SignUp(ctx, req)
	if err != nil {
		return c.JSON(http.StatusUnauthorized, map[string]string{"error": "authentication failed"})
	}

	return nil
}
