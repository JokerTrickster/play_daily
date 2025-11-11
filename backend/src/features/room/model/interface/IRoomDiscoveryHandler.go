package _interface

import "github.com/labstack/echo/v4"

type IRoomDiscoveryHandler interface {
	GetPopularRooms(c echo.Context) error
	GetRecentRooms(c echo.Context) error
}
