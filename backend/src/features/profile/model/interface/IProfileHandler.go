package _interface

import "github.com/labstack/echo/v4"

type IGetProfileHandler interface {
	GetProfile(c echo.Context) error
}

type IUpdateProfileHandler interface {
	UpdateProfile(c echo.Context) error
}
