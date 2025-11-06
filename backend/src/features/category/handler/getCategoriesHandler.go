package handler

import (
	_interface "main/features/category/model/interface"
	"net/http"

	"github.com/labstack/echo/v4"
)

type CategoryHandler struct {
	UseCase _interface.ICategoryUseCase
}

func NewCategoryHandler(c *echo.Echo, useCase _interface.ICategoryUseCase) _interface.ICategoryHandler {
	handler := &CategoryHandler{
		UseCase: useCase,
	}
	c.GET("/v0.1/categories", handler.GetCategories)
	return handler
}

// GetCategories 카테고리 목록 조회 API
// @Router /v0.1/categories [get]
// @Summary 카테고리 목록 조회 API
// @Description 모든 메모 카테고리를 조회합니다 (10개 고정, 감정별 분류)
// @Produce json
// @Success 200 {object} response.CategoriesResponse
// @Failure 500 {object} map[string]interface{}
// @Tags category
func (h *CategoryHandler) GetCategories(c echo.Context) error {
	ctx := c.Request().Context()

	categories, err := h.UseCase.GetCategories(ctx)
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{"error": err.Error()})
	}

	return c.JSON(http.StatusOK, categories)
}
