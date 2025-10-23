package handler

import (
	"main/common/db/mysql"
	"main/features/profile/repository"
	"main/features/profile/usecase"
	"time"

	"github.com/labstack/echo/v4"
)

func NewProfileHandlers(e *echo.Echo) {
	timeout := 30 * time.Second

	// GetProfile
	getProfileRepo := repository.NewGetProfileRepository(mysql.GormMysqlDB)
	getProfileUseCase := usecase.NewGetProfileUseCase(getProfileRepo, timeout)
	NewGetProfileHandler(e, getProfileUseCase)
}
