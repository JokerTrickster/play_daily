package usecase

import (
	"fmt"
	"main/common/db/mysql"
	"main/features/auth/model/request"
	"math/rand"
	"time"
)

func ValidateAuthCode(code string) bool {
	if code == "5508" {
		return true
	}
	return false
}

// generateRoomPassword generates a random 4-digit password
func generateRoomPassword() string {
	rand.Seed(time.Now().UnixNano())
	password := rand.Intn(10000) // 0-9999
	return fmt.Sprintf("%04d", password)
}

func createUserDTO(req request.ReqSignUp) *mysql.User {
	return &mysql.User{
		AccountID:    req.AccountID,
		Password:     req.Password,
		Nickname:     req.NickName,
		RoomPassword: generateRoomPassword(),
	}
}
