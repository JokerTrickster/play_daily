package _interface

import "github.com/labstack/echo/v4"

type ICategoryHandler interface {
	GetCategories(c echo.Context) error
}
