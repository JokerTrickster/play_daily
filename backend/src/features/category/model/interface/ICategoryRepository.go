package _interface

import (
	"context"
	"main/common/db/mysql"
)

type ICategoryRepository interface {
	GetAll(ctx context.Context) ([]mysql.MemoCategory, error)
}
