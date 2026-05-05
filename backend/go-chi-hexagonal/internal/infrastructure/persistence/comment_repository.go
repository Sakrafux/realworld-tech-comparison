package persistence

import (
	"context"

	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type commentRepository struct {
	db *sqlx.DB
}

func NewCommentRepository(db *sqlx.DB) port.CommentRepository {
	return &commentRepository{
		db: db,
	}
}

func (r *commentRepository) Create(ctx context.Context, comment *domain.Comment, articleID, authorID int64) error {
	query := `
		INSERT INTO comment (body, fk_article, fk_author, created_at, updated_at)
		VALUES (:body, :fk_article, :fk_author, :created_at, :updated_at)
	`
	arg := map[string]any{
		"body":       comment.Body,
		"fk_article": articleID,
		"fk_author":  authorID,
		"created_at": comment.CreatedAt,
		"updated_at": comment.UpdatedAt,
	}

	result, err := r.db.NamedExecContext(ctx, query, arg)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}
	comment.ID = id

	return nil
}
