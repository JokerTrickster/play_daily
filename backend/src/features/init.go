package features

import (
	"net/http"

	authHandler "main/features/auth/handler"
	commentHandler "main/features/comment/handler"
	memoHandler "main/features/memo/handler"
	profileHandler "main/features/profile/handler"

	"github.com/labstack/echo/v4"
)

func InitHandler(e *echo.Echo) error {

	e.GET("/health", func(c echo.Context) error {
		return c.NoContent(http.StatusOK)
	})

	authHandler.NewAuthHandler(e)
	memoHandler.NewMemoHandlers(e)
	commentHandler.NewCommentHandler(e)
	profileHandler.NewProfileHandlers(e)

	return nil
}
