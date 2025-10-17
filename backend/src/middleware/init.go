package _middleware

import (
	"github.com/gorilla/sessions"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
)

var Store = sessions.NewCookieStore([]byte("secret"))

func InitMiddleware(e *echo.Echo) error {
	e.Use(middleware.Recover())

	//cors 미들웨어 설정
	e.Use(middleware.CORSWithConfig(middleware.CORSConfig{
		AllowOrigins:     []string{"http://localhost:3000", "http://localhost:5050", "http://192.168.0.102:5050"},
		AllowMethods:     []string{echo.GET, echo.HEAD, echo.PUT, echo.PATCH, echo.POST, echo.DELETE},
		AllowHeaders:     []string{echo.HeaderOrigin, echo.HeaderContentType, echo.HeaderAccept, echo.HeaderAuthorization},
		AllowCredentials: true,
	}))

	// multipart 메시지 크기 제한 설정 (기본값: 32MB -> 2GB)
	e.Use(middleware.BodyLimit("2GB"))

	//Logger : 로깅 미들웨어
	e.Use(Logger)

	//jwt 검증 미들웨어
	// JWT 설정은 각 핸들러에서 필요시 middleware.JWTWithConfig()를 사용하여 적용
	return nil
}
