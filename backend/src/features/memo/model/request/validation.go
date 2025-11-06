package request

import (
	"fmt"
	"main/common/db/mysql"
	"regexp"

	"gorm.io/gorm"
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

// ValidateCategoryIDs 카테고리 ID 배열 검증
func ValidateCategoryIDs(db *gorm.DB, categoryIDs []uint) error {
	// 1. 카테고리 ID가 최소 1개 이상인지 확인
	if len(categoryIDs) == 0 {
		return fmt.Errorf("at least one category must be selected")
	}

	// 2. 모든 카테고리 ID가 실제로 존재하는지 확인
	var count int64
	err := db.Model(&mysql.MemoCategory{}).
		Where("id IN ?", categoryIDs).
		Count(&count).Error
	if err != nil {
		return fmt.Errorf("failed to validate category_ids: %w", err)
	}

	if int(count) != len(categoryIDs) {
		return fmt.Errorf("invalid category_ids: some categories do not exist")
	}

	return nil
}

// ValidateCreateMemo 메모 생성 요청 검증
func (r *ReqCreateMemo) ValidateCreateMemo(db *gorm.DB) error {
	// 1. 카테고리 ID 검증
	if err := ValidateCategoryIDs(db, r.CategoryIDs); err != nil {
		return err
	}

	// 2. creation_mode 별 필수 필드 검증
	if r.CreationMode == "map" {
		if r.NaverPlaceURL == nil || *r.NaverPlaceURL == "" {
			return fmt.Errorf("naver_place_url is required for map-based creation")
		}
	}

	// 3. 비즈니스 필드 검증 (기존 로직)
	if err := ValidateBusinessFields(r.BusinessName, r.BusinessPhone, r.BusinessAddress); err != nil {
		return err
	}

	return nil
}
