package utils

import (
	"crypto/rand"
	"fmt"
	"math/big"
	mathrand "math/rand"
)

// GenerateRoomPassword generates a cryptographically secure 4-digit password
func GenerateRoomPassword() string {
	max := big.NewInt(10000) // 0000-9999
	n, err := rand.Int(rand.Reader, max)
	if err != nil {
		// Fallback to math/rand if crypto/rand fails
		return fmt.Sprintf("%04d", mathrand.Intn(10000))
	}
	return fmt.Sprintf("%04d", n.Int64())
}
