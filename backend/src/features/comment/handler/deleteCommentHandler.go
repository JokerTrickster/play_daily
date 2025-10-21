package handler

import (
	_interface "main/features/comment/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type DeleteCommentHandler struct {
	UseCase _interface.IDeleteCommentUseCase
}

func NewDeleteCommentHandler(c *echo.Echo, useCase _interface.IDeleteCommentUseCase) _interface.IDeleteCommentHandler {
	handler := &DeleteCommentHandler{
		UseCase: useCase,
	}
	c.DELETE("/v0.1/comments/:comment_id", handler.Delete)
	return handler
}

// Delete 댓글 삭제 API
// @Router /v0.1/comments/{comment_id} [delete]
// @Summary 댓글 삭제 API
// @Description 댓글을 삭제합니다 (본인 댓글만 삭제 가능)
// @Produce json
// @Param comment_id path integer true "댓글 ID"
// @Success 200 {object} map[string]interface{}
// @Failure 400 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags comment
func (h *DeleteCommentHandler) Delete(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	// Path parameter에서 comment_id 추출
	commentIDStr := c.Param("comment_id")
	commentID, err := strconv.ParseUint(commentIDStr, 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid comment_id"})
	}

	// 댓글 삭제
	if err := h.UseCase.Execute(ctx, uint(commentID), userID); err != nil {
		if err.Error() == "record not found" {
			return c.JSON(http.StatusNotFound, map[string]string{"error": "comment not found or not authorized"})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, map[string]string{"message": "comment deleted successfully"})
}
