package main

import (
	"fmt"
	"main/common"
	"main/features"

	_middleware "main/middleware"

	swaggerDocs "main/docs"

	"github.com/labstack/echo/v4"

	echoSwagger "github.com/swaggo/echo-swagger"
)

//export PATH=$PATH:~/go/bin
func main() {

	e := echo.New()

	// 환경 변수 로드
	if err := common.LoadConfig(); err != nil {
		fmt.Printf("환경 변수 로드 실패: %v\n", err)
		return
	}

	// 설정 정보 출력 (디버그용)
	if common.Env.Debug {
		common.Env.Print()
	}

	if err := common.InitServer(); err != nil {
		fmt.Println(err)
		return
	}

	if err := _middleware.InitMiddleware(e); err != nil {
		fmt.Println(err)
		return
	}

	//핸드러 초기화

	if err := features.InitHandler(e); err != nil {
		fmt.Printf("handler 초기화 에러 : %v", err.Error())
		return
	}

	// swagger 초기화

	if common.Env.IsLocal {
		swaggerDocs.SwaggerInfo_swagger.Host = common.Env.Host + ":" + common.Env.Port
		e.GET("/swagger/*", echoSwagger.WrapHandler)
	} else {
		// swaggerDocs.SwaggerInfo.Host = fmt.Sprintf("dev-board-api.boardgame.com")
		e.GET("/swagger/*", echoSwagger.WrapHandler)
	}
	e.HideBanner = true
	e.Logger.Fatal(e.Start(":" + common.Env.Port))
	// e.Logger.Fatal(e.Start(":8080"))

	return
}
