package usecase

import (
	"main/common/db/mysql"
	"main/features/auth/model/request"
)

func ValidateAuthCode(code string) bool {
	if code == "5508" {
		return true
	}
	return false
}

func createUserDTO(req request.ReqSignUp) *mysql.User {
	return &mysql.User{
		AccountID: req.AccountID,
		Password:  req.Password,
		Nickname:  req.NickName,
	}
}
