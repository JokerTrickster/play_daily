package handler

import (
	_interface "main/features/comment/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type GetCommentHandler struct {
	UseCase _interface.IGetCommentUseCase
}

func NewGetCommentHandler(c *echo.Echo, useCase _interface.IGetCommentUseCase) _interface.IGetCommentHandler {
	handler := &GetCommentHandler{
		UseCase: useCase,
	}
	c.GET("/v0.1/memo/:memo_id/comments", handler.GetList)
	return handler
}

// GetList 댓글 목록 조회 API
// @Router /v0.1/memo/{memo_id}/comments [get]
// @Summary 댓글 목록 조회 API
// @Description 특정 메모의 댓글 목록을 조회합니다
// @Produce json
// @Param memo_id path integer true "메모 ID"
// @Success 200 {object} response.ResCommentList
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags comment
func (h *GetCommentHandler) GetList(c echo.Context) error {
	ctx := c.Request().Context()

	// Path parameter에서 memo_id 추출
	memoIDStr := c.Param("memo_id")
	memoID, err := strconv.ParseUint(memoIDStr, 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid memo_id"})
	}

	// 댓글 목록 조회
	comments, err := h.UseCase.ExecuteList(ctx, uint(memoID))
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, comments)
}
