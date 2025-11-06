package features

import (
	"net/http"

	authHandler "main/features/auth/handler"
	categoryHandler "main/features/category/handler"
	commentHandler "main/features/comment/handler"
	memoHandler "main/features/memo/handler"
	memolikeHandler "main/features/memolike/handler"
	migrationHandler "main/features/migration/handler"
	profileHandler "main/features/profile/handler"
	roomHandler "main/features/room/handler"
	roomlikeHandler "main/features/roomlike/handler"
	"main/common/db/mysql"

	"github.com/labstack/echo/v4"
)

func InitHandler(e *echo.Echo) error {

	e.GET("/health", func(c echo.Context) error {
		return c.NoContent(http.StatusOK)
	})

	authHandler.NewAuthHandler(e)
	categoryHandler.NewCategoryHandlers(e)
	memoHandler.NewMemoHandlers(e)
	commentHandler.NewCommentHandler(e)
	profileHandler.NewProfileHandlers(e)
	roomHandler.NewRoomHandler(e)
	roomlikeHandler.NewRoomLikeHandler(e)
	memolikeHandler.NewMemoLikeHandler(e)

	// Migration endpoint (remove after migration is complete)
	migrationHandlerInstance := migrationHandler.NewMigrationHandler(mysql.GormMysqlDB)
	e.POST("/v0.1/migration/room-members", migrationHandlerInstance.RunRoomMembersMigration)

	return nil
}
