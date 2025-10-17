package handler

import (
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/request"
	"net/http"

	"github.com/labstack/echo/v4"
)

type CreateMemoHandler struct {
	UseCase _interface.ICreateMemoUseCase
}

func NewCreateMemoHandler(c *echo.Echo, useCase _interface.ICreateMemoUseCase) _interface.ICreateMemoHandler {
	handler := &CreateMemoHandler{
		UseCase: useCase,
	}
	c.POST("/v0.1/memo", handler.CreateMemo)
	return handler
}

// CreateMemo 메모 생성 API
// @Router /v0.1/memo [post]
// @Summary 메모 생성 API
// @Description 새로운 메모를 생성합니다
// @Accept json
// @Produce json
// @Param request body request.ReqCreateMemo true "메모 생성 데이터"
// @Success 201 {object} response.ResMemo
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memo
func (h *CreateMemoHandler) CreateMemo(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	var req request.ReqCreateMemo
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request"})
	}

	memo, err := h.UseCase.CreateMemo(ctx, userID, req)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusCreated, memo)
}
