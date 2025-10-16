package _middleware

import (
	"main/common"

	"github.com/labstack/echo/v4"
)

// CheckJWT : check user's jwt token from "token" header value
func TokenChecker(next echo.HandlerFunc) echo.HandlerFunc {
	return func(c echo.Context) error {
		ctx := c.Request().Context()
		// get jwt Token
		accessToken := c.Request().Header.Get("tkn")
		if accessToken == "" {
			return common.ErrorMsg(ctx, common.ErrBadParameter, common.Trace(), "no access token in header", common.ErrFromClient)
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
