package response

type Category struct {
	ID           int    `json:"id"`
	Name         string `json:"name"`
	Sentiment    string `json:"sentiment"` // "positive", "negative", "neutral"
	Color        string `json:"color"`
	DisplayOrder int    `json:"display_order"`
}

type CategoriesResponse struct {
	Categories []Category `json:"categories"`
}
