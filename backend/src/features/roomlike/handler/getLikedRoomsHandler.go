package handler

import (
	_interface "main/features/roomlike/model/interface"
	"net/http"

	"github.com/labstack/echo/v4"
)

type GetLikedRoomsHandler struct {
	UseCase _interface.IGetLikedRoomsUseCase
}

func NewGetLikedRoomsHandler(c *echo.Echo, useCase _interface.IGetLikedRoomsUseCase) _interface.IGetLikedRoomsHandler {
	handler := &GetLikedRoomsHandler{
		UseCase: useCase,
	}
	c.GET("/v0.1/room/liked", handler.GetLikedRooms)
	return handler
}

// GetLikedRooms 좋아요한 방 목록 조회 API
// @Router /v0.1/room/liked [get]
// @Summary 좋아요한 방 목록 조회 API
// @Description 사용자가 좋아요한 방 목록을 조회합니다
// @Accept json
// @Produce json
// @Success 200 {object} []response.ResLikedRoom
// @Failure 500 {object} map[string]interface{}
// @Tags roomlike
func (h *GetLikedRoomsHandler) GetLikedRooms(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	// UseCase 실행
	likedRooms, err := h.UseCase.Execute(ctx, userID)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, likedRooms)
}
