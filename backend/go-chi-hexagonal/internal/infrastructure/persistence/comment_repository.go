package persistence

import (
	"context"
	"database/sql"
	"time"

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
		RETURNING id
	`
	arg := map[string]any{
		"body":       comment.Body,
		"fk_article": articleID,
		"fk_author":  authorID,
		"created_at": comment.CreatedAt,
		"updated_at": comment.UpdatedAt,
	}

	var id int64
	stmt, err := r.db.PrepareNamedContext(ctx, query)
	if err != nil {
		return err
	}
	defer stmt.Close()

	err = stmt.GetContext(ctx, &id, arg)
	if err != nil {
		return err
	}
	comment.ID = id

	return nil
}

type commentSchema struct {
	ID        int64     `db:"id"`
	Body      string    `db:"body"`
	CreatedAt time.Time `db:"created_at"`
	UpdatedAt time.Time `db:"updated_at"`
	ArticleID int64     `db:"fk_article"`
	AuthorID  int64     `db:"fk_author"`
	// Joined fields
	AuthorUsername string         `db:"username"`
	AuthorBio      string         `db:"bio"`
	AuthorImage    sql.NullString `db:"image"`
	Following      bool           `db:"following"`
}

func (s *commentSchema) toDomain() domain.Comment {
	var image *string
	if s.AuthorImage.Valid {
		image = &s.AuthorImage.String
	}

	return domain.Comment{
		ID:        s.ID,
		Body:      s.Body,
		CreatedAt: s.CreatedAt,
		UpdatedAt: s.UpdatedAt,
		Author: domain.Profile{
			Username:  s.AuthorUsername,
			Bio:       s.AuthorBio,
			Image:     image,
			Following: s.Following,
		},
	}
}

func (r *commentRepository) GetByID(ctx context.Context, id int64) (*domain.Comment, int64, int64, error) {
	var schema commentSchema
	query := `
		SELECT c.id, c.body, c.created_at, c.updated_at, c.fk_article, c.fk_author,
		       u.username, u.bio, u.image,
		       0 as following
		FROM comment c
		JOIN app_user u ON c.fk_author = u.id
		WHERE c.id = $1
	`
	err := r.db.GetContext(ctx, &schema, query, id)
	if err == sql.ErrNoRows {
		return nil, 0, 0, nil
	}
	if err != nil {
		return nil, 0, 0, err
	}

	comment := schema.toDomain()
	return &comment, schema.ArticleID, schema.AuthorID, nil
}

func (r *commentRepository) Delete(ctx context.Context, id int64) error {
	query := `DELETE FROM comment WHERE id = $1`
	_, err := r.db.ExecContext(ctx, query, id)
	return err
}

func (r *commentRepository) FindByArticleID(ctx context.Context, articleID int64, observerID *int64) ([]domain.Comment, error) {
	var schemas []commentSchema
	var query string
	var args []any

	if observerID != nil {
		query = `
			SELECT c.id, c.body, c.created_at, c.updated_at, c.fk_article, c.fk_author,
			       u.username, u.bio, u.image,
			       CASE WHEN f.following_user_id IS NOT NULL THEN 1 ELSE 0 END as following
			FROM comment c
			JOIN app_user u ON c.fk_author = u.id
			LEFT JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $2
			WHERE c.fk_article = $1
			ORDER BY c.created_at DESC
		`
		args = []any{articleID, *observerID}
	} else {
		query = `
			SELECT c.id, c.body, c.created_at, c.updated_at, c.fk_article, c.fk_author,
			       u.username, u.bio, u.image,
			       0 as following
			FROM comment c
			JOIN app_user u ON c.fk_author = u.id
			WHERE c.fk_article = $1
			ORDER BY c.created_at DESC
		`
		args = []any{articleID}
	}

	err := r.db.SelectContext(ctx, &schemas, query, args...)
	if err != nil {
		return nil, err
	}

	comments := make([]domain.Comment, len(schemas))
	for i, s := range schemas {
		comments[i] = s.toDomain()
	}

	return comments, nil
}
