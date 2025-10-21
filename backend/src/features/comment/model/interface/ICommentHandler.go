package _interface

import "github.com/labstack/echo/v4"

type ICreateCommentHandler interface {
	Create(c echo.Context) error
}

type IGetCommentHandler interface {
	GetList(c echo.Context) error
}

type IDeleteCommentHandler interface {
	Delete(c echo.Context) error
}
