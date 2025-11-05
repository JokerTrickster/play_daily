package handler

import (
	_interface "main/features/room/model/interface"
	"net/http"
	"strconv"

	"github.com/labstack/echo/v4"
)

type GetRoomMembersHandler struct {
	GetRoomMembersUseCase _interface.IGetRoomMembersUseCase
}

func NewGetRoomMembersHandler(useCase _interface.IGetRoomMembersUseCase) _interface.IGetRoomMembersHandler {
	return &GetRoomMembersHandler{
		GetRoomMembersUseCase: useCase,
	}
}

// GetRoomMembers 방 멤버 목록 조회 API
func (h *GetRoomMembersHandler) GetRoomMembers(c echo.Context) error {
	ctx := c.Request().Context()

	// 현재 로그인한 사용자 ID 가져오기
	userID, ok := c.Get("uID").(uint)
	if !ok {
		return c.JSON(http.StatusUnauthorized, map[string]string{"error": "unauthorized"})
	}

	// room_id 파라미터 가져오기
	roomIDStr := c.QueryParam("room_id")
	if roomIDStr == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "room_id is required"})
	}

	roomID, err := strconv.ParseUint(roomIDStr, 10, 32)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid room_id"})
	}

	// UseCase 호출
	members, err := h.GetRoomMembersUseCase.GetRoomMembers(ctx, userID, uint(roomID))
	if err != nil {
		if err.Error() == "방 멤버만 멤버 목록을 조회할 수 있습니다" {
			return c.JSON(http.StatusForbidden, map[string]string{"error": "only room members can view member list"})
		}
		if err.Error() == "멤버 목록 조회에 실패했습니다" {
			return c.JSON(http.StatusNotFound, map[string]string{"error": "room not found"})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": "internal server error"})
	}

	return c.JSON(http.StatusOK, members)
}
