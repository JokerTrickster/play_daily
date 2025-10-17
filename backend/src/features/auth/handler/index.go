package handler

import (
	"main/features/auth/repository"
	"main/features/auth/usecase"
	"time"

	"main/common/db/mysql"

	"github.com/labstack/echo/v4"
)

func NewAuthHandler(e *echo.Echo) {
	NewSignInAuthHandler(e, usecase.NewSignInAuthUseCase(repository.NewSignInAuthRepository(mysql.GormMysqlDB), 30*time.Second))
	NewSignUpAuthHandler(e, usecase.NewSignUpAuthUseCase(repository.NewSignUpAuthRepository(mysql.GormMysqlDB), 30*time.Second))
}
