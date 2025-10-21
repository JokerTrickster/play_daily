package handler

import (
	"main/common/db/mysql"
	"main/features/comment/repository"
	"main/features/comment/usecase"

	"github.com/labstack/echo/v4"
)

func NewCommentHandler(c *echo.Echo) {
	db := mysql.GormMysqlDB

	// Repository 초기화
	createCommentRepo := repository.NewCreateCommentRepository(db)
	getCommentRepo := repository.NewGetCommentRepository(db)
	deleteCommentRepo := repository.NewDeleteCommentRepository(db)

	// UseCase 초기화
	createCommentUseCase := usecase.NewCreateCommentUseCase(createCommentRepo)
	getCommentUseCase := usecase.NewGetCommentUseCase(getCommentRepo)
	deleteCommentUseCase := usecase.NewDeleteCommentUseCase(deleteCommentRepo)

	// Handler 초기화 (라우트 자동 등록)
	NewCreateCommentHandler(c, createCommentUseCase)
	NewGetCommentHandler(c, getCommentUseCase)
	NewDeleteCommentHandler(c, deleteCommentUseCase)
}
