package repository

import (
	"context"
	"main/common/db/mysql"
	_interface "main/features/comment/model/interface"

	"gorm.io/gorm"
)

type CreateCommentRepository struct {
	GormDB *gorm.DB
}

func NewCreateCommentRepository(gormDB *gorm.DB) _interface.ICreateCommentRepository {
	return &CreateCommentRepository{
		GormDB: gormDB,
	}
}

func (r *CreateCommentRepository) Create(ctx context.Context, comment *mysql.Comment) error {
	result := r.GormDB.WithContext(ctx).Create(comment)
	return result.Error
}
