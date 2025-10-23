package handler

import (
	_interface "main/features/profile/model/interface"
	"main/features/profile/model/request"
	"net/http"

	"github.com/labstack/echo/v4"
)

type UpdateProfileHandler struct {
	UseCase _interface.IUpdateProfileUseCase
}

func NewUpdateProfileHandler(c *echo.Echo, useCase _interface.IUpdateProfileUseCase) _interface.IUpdateProfileHandler {
	handler := &UpdateProfileHandler{
		UseCase: useCase,
	}
	c.PUT("/v0.1/profile", handler.UpdateProfile)
	return handler
}

// UpdateProfile 프로필 업데이트 API
// @Router /v0.1/profile [put]
// @Summary 프로필 업데이트 API
// @Description 사용자의 프로필 정보를 업데이트합니다
// @Accept json
// @Produce json
// @Param req body request.ReqUpdateProfile true "Update Profile Request"
// @Success 200 {object} response.ResProfile
// @Failure 400 {object} map[string]interface{}
// @Failure 404 {object} map[string]interface{}
// @Failure 500 {object} map[string]interface{}
// @Tags profile
func (h *UpdateProfileHandler) UpdateProfile(c echo.Context) error {
	ctx := c.Request().Context()

	// TODO: JWT에서 userID 추출
	userID := uint(1)

	// 요청 바인딩
	var req request.ReqUpdateProfile
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "invalid request format"})
	}

	// 필수 필드 검증
	if req.CurrentPassword == "" {
		return c.JSON(http.StatusBadRequest, map[string]string{"error": "current_password is required"})
	}

	// 프로필 업데이트
	profile, err := h.UseCase.UpdateProfile(ctx, userID, req)
	if err != nil {
		// 비밀번호 불일치 에러
		if err.Error() == "incorrect current password" {
			return c.JSON(http.StatusBadRequest, map[string]string{"error": "incorrect current password"})
		}
		// 사용자 없음 에러
		if err.Error() == "user not found" {
			return c.JSON(http.StatusNotFound, map[string]string{"error": "user not found"})
		}
		// 기타 서버 에러
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, profile)
}
