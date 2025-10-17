package handler

import (
	"main/common/db/mysql"
	"main/features/memo/repository"
	"main/features/memo/usecase"
	"time"

	"github.com/labstack/echo/v4"
)

func NewMemoHandlers(e *echo.Echo) {
	timeout := 30 * time.Second

	// Create
	createRepo := repository.NewCreateMemoRepository(mysql.GormMysqlDB)
	createUseCase := usecase.NewCreateMemoUseCase(createRepo, timeout)
	NewCreateMemoHandler(e, createUseCase)

	// Get
	getRepo := repository.NewGetMemoRepository(mysql.GormMysqlDB)
	getUseCase := usecase.NewGetMemoUseCase(getRepo, timeout)
	NewGetMemoHandler(e, getUseCase)

	// Update
	updateRepo := repository.NewUpdateMemoRepository(mysql.GormMysqlDB)
	updateUseCase := usecase.NewUpdateMemoUseCase(updateRepo, timeout)
	NewUpdateMemoHandler(e, updateUseCase)

	// Delete
	deleteRepo := repository.NewDeleteMemoRepository(mysql.GormMysqlDB)
	deleteUseCase := usecase.NewDeleteMemoUseCase(deleteRepo, timeout)
	NewDeleteMemoHandler(e, deleteUseCase)
}
