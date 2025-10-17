package handler

import (
	_interface "main/features/memo/model/interface"
	"main/features/memo/model/request"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type UpdateMemoHandler struct {
	UseCase _interface.IUpdateMemoUseCase
}

func NewUpdateMemoHandler(c *echo.Echo, useCase _interface.IUpdateMemoUseCase) _interface.IUpdateMemoHandler {
	handler := &UpdateMemoHandler{
		UseCase: useCase,
	}
	c.PUT("/v0.1/memo/:id", handler.UpdateMemo)
	return handler
}

// UpdateMemo 메모 수정 API
// @Router /v0.1/memo/{id} [put]
// @Summary 메모 수정 API
// @Description 메모를 수정합니다
// @Accept json
// @Produce json
// @Param id path int true "메모 ID"
// @Param request body request.ReqUpdateMemo true "메모 수정 데이터"
// @Success 200 {object} response.ResMemo
// @Failure 400 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memo
func (h *UpdateMemoHandler) UpdateMemo(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid memo id"})
	}

	var req request.ReqUpdateMemo
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request"})
	}

	memo, err := h.UseCase.UpdateMemo(ctx, uint(id), userID, req)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, memo)
}
