package persistence

import (
	"context"

	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type tagRepository struct {
	db *sqlx.DB
}

func NewTagRepository(db *sqlx.DB) port.TagRepository {
	return &tagRepository{
		db: db,
	}
}

// FindAll retrieves all tags from the database.
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
