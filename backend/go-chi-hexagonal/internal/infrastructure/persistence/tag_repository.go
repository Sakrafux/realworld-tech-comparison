package persistence

import (
	"context"

	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/jmoiron/sqlx"
)

type tagRepository struct {
	db *sqlx.DB
}

func NewPostgresTagRepository(db *sqlx.DB) port.TagRepository {
	return &tagRepository{
		db: db,
	}
}

func (r *tagRepository) FindAll(ctx context.Context) ([]domain.Tag, error) {
	var tagNames []string
	query := `SELECT tag FROM tag`
	err := r.db.SelectContext(ctx, &tagNames, query)
	if err != nil {
		return nil, err
	}

	tags := make([]domain.Tag, len(tagNames))
	for i, name := range tagNames {
		tags[i] = domain.Tag{Name: name}
	}

	return tags, nil
}
