package _interface

import "github.com/labstack/echo/v4"

type IReissueAuthHandler interface {
	Reissue(c echo.Context) error
}
