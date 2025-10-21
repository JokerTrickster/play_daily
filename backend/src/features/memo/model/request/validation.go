package request

import (
	"fmt"
	"regexp"
)

// 전화번호 검증용 정규식
var phoneRegex = regexp.MustCompile(`^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$`)

// ValidateBusinessFields 비즈니스 정보 필드 검증
func ValidateBusinessFields(businessName, businessPhone, businessAddress *string) error {
	// businessName 검증
	if businessName != nil && len(*businessName) > 255 {
		return fmt.Errorf("business_name exceeds maximum length of 255 characters")
	}

	// businessPhone 검증
	if businessPhone != nil && *businessPhone != "" {
		if !phoneRegex.MatchString(*businessPhone) {
			return fmt.Errorf("business_phone format is invalid (expected format: +82-10-1234-5678 or 010-1234-5678)")
		}
	}

	// businessAddress 검증
	if businessAddress != nil && len(*businessAddress) > 1000 {
		return fmt.Errorf("business_address exceeds maximum length of 1000 characters")
	}

	return nil
}
