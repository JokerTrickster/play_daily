package features

import (
	"net/http"

	authHandler "main/features/auth/handler"

	"github.com/labstack/echo/v4"
)

func InitHandler(e *echo.Echo) error {

	e.GET("/health", func(c echo.Context) error {
		return c.NoContent(http.StatusOK)
	})

	authHandler.NewAuthHandler(e)

	return nil
}
