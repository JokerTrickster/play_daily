package handler

import (
	_interface "main/features/memolike/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type ToggleMemoLikeHandler struct {
	UseCase _interface.IToggleMemoLikeUseCase
}

func NewToggleMemoLikeHandler(c *echo.Echo, useCase _interface.IToggleMemoLikeUseCase) _interface.IToggleMemoLikeHandler {
	handler := &ToggleMemoLikeHandler{
		UseCase: useCase,
	}
	c.POST("/v0.1/memo/:memo_id/like", handler.ToggleLike)
	return handler
}

// ToggleLike 메모 좋아요 토글 API
// @Router /v0.1/memo/{memo_id}/like [post]
// @Summary 메모 좋아요 토글 API
// @Description 메모에 좋아요를 추가하거나 제거합니다 (토글)
// @Accept json
// @Produce json
// @Param memo_id path integer true "메모 ID"
// @Success 200 {object} map[string]interface{} "좋아요 상태 반환 (is_liked: true/false)"
// @Failure 400 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags memolike
func (h *ToggleMemoLikeHandler) ToggleLike(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	// Path parameter에서 memo_id 추출
	memoIDStr := c.Param("memo_id")
	memoID, err := strconv.ParseUint(memoIDStr, 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid memo_id"})
	}

	// UseCase 실행 (토글)
	isLiked, err := h.UseCase.Execute(ctx, uint(memoID), userID)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	// 토글 결과 반환
	return c.JSON(http.StatusOK, map[string]interface{}{
		"is_liked": isLiked,
		"message":  func() string {
			if isLiked {
				return "memo liked successfully"
			}
			return "memo unliked successfully"
		}(),
	})
}
