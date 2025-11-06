package _middleware

import (
	"main/common"

	"github.com/labstack/echo/v4"
)

// CheckJWT : check user's jwt token from "token" header value
func TokenChecker(next echo.HandlerFunc) echo.HandlerFunc {
	return func(c echo.Context) error {
		// get jwt Token
		accessToken := c.Request().Header.Get("tkn")
		println("TokenChecker - tkn header:", accessToken)
		println("TokenChecker - all headers:", c.Request().Header)

		// TEMPORARY: Allow requests without token for development (default to user ID 1)
		if accessToken == "" {
			println("TokenChecker - WARNING: No token, using default user ID 1 for development")
			c.Set("uID", uint(1))
			c.Set("email", "a")
			return next(c)
		}

		// verify & get Data
		err := common.VerifyToken(accessToken)
		if err != nil {
			return err
		}
		uID, email, err := common.ParseToken(accessToken)
		if err != nil {
			return err
		}

		// set token data to Context
		c.Set("uID", uID)
		c.Set("email", email)

		return next(c)

	}
}
