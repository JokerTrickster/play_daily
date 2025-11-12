package _middleware

import (
	"main/common"
	"main/common/db/mysql"

	"github.com/labstack/echo/v4"
)

// CheckJWT : check user's jwt token from "token" header value
func TokenChecker(next echo.HandlerFunc) echo.HandlerFunc {
	return func(c echo.Context) error {
		// get jwt Token
		accessToken := c.Request().Header.Get("tkn")
		println("TokenChecker - tkn header:", accessToken)
		println("TokenChecker - all headers:", c.Request().Header)

		// Return 401 if no token provided
		if accessToken == "" {
			return common.ErrorMsg(c.Request().Context(), common.ErrBadToken, common.Trace(), "access token required", common.ErrFromClient)
		}

		// verify JWT signature
		err := common.VerifyToken(accessToken)
		if err != nil {
			return err
		}

		// validate token exists in database and is not revoked
		token, err := common.GetTokenByAccessToken(mysql.GormMysqlDB, accessToken)
		if err != nil {
			return err
		}

		// parse token claims
		uID, email, err := common.ParseToken(accessToken)
		if err != nil {
			return err
		}

		// verify user ID matches token record
		if token.UserID != uID {
			return common.ErrorMsg(c.Request().Context(), common.ErrBadToken, common.Trace(), "token user mismatch", common.ErrFromClient)
		}

		// set token data to Context
		c.Set("uID", uID)
		c.Set("email", email)

		return next(c)

	}
}
