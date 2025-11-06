package handler

import (
	"main/common/db/mysql"
	"main/features/category/repository"
	"main/features/category/usecase"
	"time"

	"github.com/labstack/echo/v4"
)

func NewCategoryHandlers(e *echo.Echo) {
	timeout := 30 * time.Second

	// Get Categories
	repo := repository.NewCategoryRepository(mysql.GormMysqlDB)
	uc := usecase.NewCategoryUseCase(repo, timeout)
	NewCategoryHandler(e, uc)
}
