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

type GetArticleQuery struct {
	Slug       string
	ObserverID *int64
}

// ArticleService defines the inbound port for article-related use cases.
type ArticleService interface {
	CreateArticle(ctx context.Context, cmd CreateArticleCommand) (*domain.Article, error)
	GetArticle(ctx context.Context, query GetArticleQuery) (*domain.Article, error)
}

// ArticleRepository defines the outbound port for article data persistence.
type ArticleRepository interface {
	Create(ctx context.Context, article *domain.Article, authorID int64) error
	GetBySlug(ctx context.Context, slug string, observerID *int64) (*domain.Article, error)
	GetByTitle(ctx context.Context, title string, observerID *int64) (*domain.Article, error)
}
