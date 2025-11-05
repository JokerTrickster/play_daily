package handler

import (
	_interface "main/features/room/model/interface"
	"main/features/room/model/request"
	"net/http"

	"github.com/labstack/echo/v4"
)

type KickUserHandler struct {
	KickUserUseCase _interface.IKickUserUseCase
}

func NewKickUserHandler(kickUserUseCase _interface.IKickUserUseCase) _interface.IKickUserHandler {
	return &KickUserHandler{
		KickUserUseCase: kickUserUseCase,
	}
}

// KickUser 사용자 추방 API
func (h *KickUserHandler) KickUser(c echo.Context) error {
	ctx := c.Request().Context()

	// 현재 로그인한 사용자 ID 가져오기
	userID, ok := c.Get("uID").(uint)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{"error": "unauthorized"})
	}

	// 요청 바인딩
	var req request.ReqKickUser
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request"})
	}

	// 요청 검증
	if req.RoomID == 0 {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "room_id is required"})
	}
	if req.TargetUserID == 0 {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "target_user_id is required"})
	}

	// UseCase 호출
	err := h.KickUserUseCase.KickUser(ctx, userID, &req)
	if err != nil {
		if err.Error() == "방장만 사용자를 추방할 수 있습니다" {
			return c.JSON(http.StatusForbidden, map[string]string{"error": "only room owner can kick users"})
		}
		if err.Error() == "방장은 자신을 추방할 수 없습니다" {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": "owner cannot kick themselves"})
		}
		if err.Error() == "사용자 추방에 실패했습니다" {
			return c.JSON(http.StatusNotFound, map[string]string{"error": "user not found in room"})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "internal server error"})
	}

	return c.JSON(http.StatusOK, map[string]string{"message": "user successfully kicked from room"})
}
