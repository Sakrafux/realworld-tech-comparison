package port

import (
	"context"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

// CreateArticleCommand represents the data needed to create a new article.
type CreateArticleCommand struct {
	AuthorID    int64
	Title       string
	Description string
	Body        string
	TagList     []string
}

// UpdateArticleCommand represents the data needed to update an existing article.
type UpdateArticleCommand struct {
	Slug        string
	UserID      int64
	Title       *string
	Description *string
	Body        *string
}

type GetArticleQuery struct {
	Slug       string
	ObserverID *int64
}

// ArticleService defines the inbound port for article-related use cases.
type ArticleService interface {
	CreateArticle(ctx context.Context, cmd CreateArticleCommand) (*domain.Article, error)
	GetArticle(ctx context.Context, query GetArticleQuery) (*domain.Article, error)
	UpdateArticle(ctx context.Context, cmd UpdateArticleCommand) (*domain.Article, error)
	DeleteArticle(ctx context.Context, slug string, userID int64) error
}

// ArticleRepository defines the outbound port for article data persistence.
type ArticleRepository interface {
	Create(ctx context.Context, article *domain.Article, authorID int64) error
	GetBySlug(ctx context.Context, slug string, observerID *int64) (*domain.Article, error)
	GetByTitle(ctx context.Context, title string, observerID *int64) (*domain.Article, error)
	Update(ctx context.Context, article *domain.Article) error
	Delete(ctx context.Context, id int64) error
}
