package _interface

import "github.com/labstack/echo/v4"

type ICreateMemoHandler interface {
	CreateMemo(c echo.Context) error
}

type IGetMemoHandler interface {
	GetMemo(c echo.Context) error
	GetMemoList(c echo.Context) error
}

type IUpdateMemoHandler interface {
	UpdateMemo(c echo.Context) error
}

type IDeleteMemoHandler interface {
	DeleteMemo(c echo.Context) error
}
