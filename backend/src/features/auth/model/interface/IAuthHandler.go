package _interface

import "github.com/labstack/echo/v4"

type ISignInAuthHandler interface {
	SignIn(c echo.Context) error
}
