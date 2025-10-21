package handler

import (
	_interface "main/features/comment/model/interface"
	"main/features/comment/model/request"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type CreateCommentHandler struct {
	UseCase _interface.ICreateCommentUseCase
}

func NewCreateCommentHandler(c *echo.Echo, useCase _interface.ICreateCommentUseCase) _interface.ICreateCommentHandler {
	handler := &CreateCommentHandler{
		UseCase: useCase,
	}
	c.POST("/v0.1/memo/:memo_id/comments", handler.Create)
	return handler
}

// Create 댓글 생성 API
// @Router /v0.1/memo/{memo_id}/comments [post]
// @Summary 댓글 생성 API
// @Description 메모에 댓글을 작성합니다
// @Accept json
// @Produce json
// @Param memo_id path integer true "메모 ID"
// @Param body body request.ReqCreateComment true "댓글 내용"
// @Success 201 {object} response.ResComment
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags comment
func (h *CreateCommentHandler) Create(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	// Path parameter에서 memo_id 추출
	memoIDStr := c.Param("memo_id")
	memoID, err := strconv.ParseUint(memoIDStr, 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid memo_id"})
	}

	// Request body 파싱
	var req request.ReqCreateComment
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request body"})
	}

	// 댓글 생성
	comment, err := h.UseCase.Execute(ctx, uint(memoID), userID, &req)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusCreated, comment)
}
