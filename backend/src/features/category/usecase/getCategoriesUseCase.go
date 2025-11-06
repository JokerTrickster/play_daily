package usecase

import (
	"context"
	_interface "main/features/category/model/interface"
	"main/features/category/model/response"
	"sync"
	"time"
)

var (
	cachedCategories *response.CategoriesResponse
	cacheMutex       sync.RWMutex
	cacheInitialized bool
)

type CategoryUseCase struct {
	Repository     _interface.ICategoryRepository
	ContextTimeout time.Duration
}

func NewCategoryUseCase(repo _interface.ICategoryRepository, timeout time.Duration) _interface.ICategoryUseCase {
	return &CategoryUseCase{
		Repository:     repo,
		ContextTimeout: timeout,
	}
}

// GetCategories 모든 카테고리 조회 (캐싱 적용)
func (uc *CategoryUseCase) GetCategories(ctx context.Context) (*response.CategoriesResponse, error) {
	// Read lock으로 캐시 확인
	cacheMutex.RLock()
	if cacheInitialized {
		defer cacheMutex.RUnlock()
		return cachedCategories, nil
	}
	cacheMutex.RUnlock()

	// Write lock 획득
	cacheMutex.Lock()
	defer cacheMutex.Unlock()

	// Double-check: 다른 고루틴이 이미 초기화했을 수 있음
	if cacheInitialized {
		return cachedCategories, nil
	}

	// 컨텍스트 타임아웃 설정
	ctx, cancel := context.WithTimeout(ctx, uc.ContextTimeout)
	defer cancel()

	// DB에서 카테고리 조회
	categories, err := uc.Repository.GetAll(ctx)
	if err != nil {
		return nil, err
	}

	// 응답 모델로 변환
	responseCategories := make([]response.Category, len(categories))
	for i, cat := range categories {
		responseCategories[i] = response.Category{
			ID:           int(cat.ID),
			Name:         cat.NameKo,
			Sentiment:    cat.Sentiment,
			Color:        cat.ColorHex,
			DisplayOrder: cat.DisplayOrder,
		}
	}

	// 캐시에 저장
	cachedCategories = &response.CategoriesResponse{
		Categories: responseCategories,
	}
	cacheInitialized = true

	return cachedCategories, nil
}
