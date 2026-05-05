package persistence

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type articleRepository struct {
	db *sqlx.DB
}

func NewArticleRepository(db *sqlx.DB) port.ArticleRepository {
	return &articleRepository{
		db: db,
	}
}

type articleSchema struct {
	ID          int64     `db:"id"`
	Slug        string    `db:"slug"`
	Title       string    `db:"title"`
	Description string    `db:"description"`
	Body        string    `db:"body"`
	AuthorID    int64     `db:"fk_author"`
	CreatedAt   time.Time `db:"created_at"`
	UpdatedAt   time.Time `db:"updated_at"`
	// Joined fields
	AuthorUsername string         `db:"username"`
	AuthorBio      string         `db:"bio"`
	AuthorImage    sql.NullString `db:"image"`
	Following      bool           `db:"following"`
}

func (s *articleSchema) toDomain(tags []string) *domain.Article {
	var image *string
	if s.AuthorImage.Valid {
		image = &s.AuthorImage.String
	}

	return &domain.Article{
		ID:             s.ID,
		Slug:           s.Slug,
		Title:          s.Title,
		Description:    s.Description,
		Body:           s.Body,
		TagList:        tags,
		CreatedAt:      s.CreatedAt,
		UpdatedAt:      s.UpdatedAt,
		Favorited:      false, // TODO: implement favorites check if auth user
		FavoritesCount: 0,     // TODO: implement favorites count
		Author: domain.Profile{
			Username:  s.AuthorUsername,
			Bio:       s.AuthorBio,
			Image:     image,
			Following: s.Following,
		},
	}
}

func (r *articleRepository) Create(ctx context.Context, article *domain.Article, authorID int64) error {
	tx, err := r.db.BeginTxx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	query := `
		INSERT INTO article (slug, title, description, body, fk_author, created_at, updated_at)
		VALUES (:slug, :title, :description, :body, :fk_author, :created_at, :updated_at)
	`
	arg := map[string]any{
		"slug":        article.Slug,
		"title":       article.Title,
		"description": article.Description,
		"body":        article.Body,
		"fk_author":   authorID,
		"created_at":  article.CreatedAt,
		"updated_at":  article.UpdatedAt,
	}

	result, err := tx.NamedExecContext(ctx, query, arg)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}
	article.ID = id

	if len(article.TagList) > 0 {
		err = r.createTags(ctx, tx, article)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

type tagRow struct {
	Tag string `db:"tag"`
}

type tagArticleRelationRow struct {
	ArticleId int64 `db:"article_id"`
	TagId     int64 `db:"tag_id"`
}

func (r *articleRepository) createTags(ctx context.Context, tx *sqlx.Tx, article *domain.Article) error {
	// 1. Insert all tags regardless of existence
	tagRows := make([]tagRow, len(article.TagList))
	for i, t := range article.TagList {
		tagRows[i] = tagRow{Tag: t}
	}

	_, err := tx.NamedExecContext(ctx, `
		INSERT INTO tag (tag) 
		VALUES (:tag) 
		ON CONFLICT (tag) DO NOTHING
	`, tagRows)
	if err != nil {
		return err
	}

	// 2. Get all tag IDs in one query
	query, args, err := sqlx.In("SELECT id FROM tag WHERE tag IN (?)", article.TagList)
	if err != nil {
		return err
	}
	query = tx.Rebind(query)

	var tagIDs []int64
	err = tx.SelectContext(ctx, &tagIDs, query, args...)
	if err != nil {
		return err
	}

	// 3. Bulk insert of the associations
	relRows := make([]tagArticleRelationRow, len(tagIDs))
	for i, id := range tagIDs {
		relRows[i] = tagArticleRelationRow{ArticleId: article.ID, TagId: id}
	}

	_, err = tx.NamedExecContext(ctx, `
			INSERT INTO tag_is_article_to_tag (article_id, tag_id)
			VALUES (:article_id, :tag_id)
        ON CONFLICT (article_id, tag_id) DO NOTHING
        `, relRows)
	if err != nil {
		return err
	}

	return nil
}

func (r *articleRepository) GetBySlug(ctx context.Context, slug string, observerID *int64) (*domain.Article, error) {
	return r.findOneBy(ctx, "slug", slug, observerID)
}

func (r *articleRepository) GetByTitle(ctx context.Context, title string, observerID *int64) (*domain.Article, error) {
	return r.findOneBy(ctx, "title", title, observerID)
}

func (r *articleRepository) Update(ctx context.Context, article *domain.Article) error {
	query := `
		UPDATE article 
		SET slug = :slug, title = :title, description = :description, body = :body, updated_at = :updated_at
		WHERE id = :id
	`
	arg := map[string]any{
		"id":          article.ID,
		"slug":        article.Slug,
		"title":       article.Title,
		"description": article.Description,
		"body":        article.Body,
		"updated_at":  article.UpdatedAt,
	}

	_, err := r.db.NamedExecContext(ctx, query, arg)
	return err
}

func (r *articleRepository) Delete(ctx context.Context, id int64) error {
	tx, err := r.db.BeginTxx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	// 1. Delete tag associations
	_, err = tx.ExecContext(ctx, "DELETE FROM tag_is_article_to_tag WHERE article_id = $1", id)
	if err != nil {
		return err
	}

	// 2. Delete favorites
	_, err = tx.ExecContext(ctx, "DELETE FROM favorite_is_article_to_user WHERE article_id = $1", id)
	if err != nil {
		return err
	}

	// 3. Delete comments
	_, err = tx.ExecContext(ctx, "DELETE FROM comment WHERE fk_article = $1", id)
	if err != nil {
		return err
	}

	// 4. Delete article
	_, err = tx.ExecContext(ctx, "DELETE FROM article WHERE id = $1", id)
	if err != nil {
		return err
	}

	return tx.Commit()
}

func (r *articleRepository) findOneBy(ctx context.Context, column string, value any, observerID *int64) (*domain.Article, error) {
	var schema articleSchema
	var query string
	var args []any

	if observerID != nil {
		query = `
			SELECT a.id, a.slug, a.title, a.description, a.body, a.fk_author, a.created_at, a.updated_at,
			       u.username, u.bio, u.image,
			       CASE WHEN f.following_user_id IS NOT NULL THEN 1 ELSE 0 END as following
			FROM article a
			JOIN app_user u ON a.fk_author = u.id
			LEFT JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $2
			WHERE a.` + column + ` = $1
		`
		args = []any{value, *observerID}
	} else {
		query = `
			SELECT a.id, a.slug, a.title, a.description, a.body, a.fk_author, a.created_at, a.updated_at,
			       u.username, u.bio, u.image,
			       0 as following
			FROM article a
			JOIN app_user u ON a.fk_author = u.id
			WHERE a.` + column + ` = $1
		`
		args = []any{value}
	}

	err := r.db.GetContext(ctx, &schema, query, args...)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}

	var tags []string
	err = r.db.SelectContext(ctx, &tags, `
		SELECT t.tag 
		FROM tag t
		JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id
		WHERE tat.article_id = $1
	`, schema.ID)
	if err != nil {
		return nil, err
	}

	return schema.toDomain(tags), nil
}
