package handler

import (
	"main/common/db/mysql"
	"main/features/memolike/repository"
	"main/features/memolike/usecase"
	"time"

	"github.com/labstack/echo/v4"
)

func NewMemoLikeHandler(e *echo.Echo) {
	timeout := 30 * time.Second

	// ToggleMemoLike
	toggleRepo := repository.NewToggleMemoLikeRepository(mysql.GormMysqlDB)
	toggleUseCase := usecase.NewToggleMemoLikeUseCase(toggleRepo, timeout)
	NewToggleMemoLikeHandler(e, toggleUseCase)
}
