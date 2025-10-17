package handler

import (
	_interface "main/features/memo/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type DeleteMemoHandler struct {
	UseCase _interface.IDeleteMemoUseCase
}

func NewDeleteMemoHandler(c *echo.Echo, useCase _interface.IDeleteMemoUseCase) _interface.IDeleteMemoHandler {
	handler := &DeleteMemoHandler{
		UseCase: useCase,
	}
	c.DELETE("/v0.1/memo/:id", handler.DeleteMemo)
	return handler
}

// DeleteMemo 메모 삭제 API
// @Router /v0.1/memo/{id} [delete]
// @Summary 메모 삭제 API
// @Description 메모를 삭제합니다
// @Param id path int true "메모 ID"
// @Success 204 "No Content"
// @Failure 400 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memo
func (h *DeleteMemoHandler) DeleteMemo(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid memo id"})
	}

	err = h.UseCase.DeleteMemo(ctx, uint(id), userID)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.NoContent(http.StatusNoContent)
}
