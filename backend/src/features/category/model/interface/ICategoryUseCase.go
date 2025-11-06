package _interface

import (
	"context"
	"main/features/category/model/response"
)

type ICategoryUseCase interface {
	GetCategories(ctx context.Context) (*response.CategoriesResponse, error)
}
