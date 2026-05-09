package article

import (
	"context"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/cells/user"
)

// --- Commands & Queries ---

type CreateArticleCommand struct {
	AuthorID    int64
	Title       string
	Description string
	Body        string
	TagList     []string
}

type UpdateArticleCommand struct {
	Slug        string
	UserID      int64
	Title       *string
	Description *string
	Body        *string
}

type DeleteArticleCommand struct {
	Slug   string
	UserID int64
}

type FavoriteArticleCommand struct {
	Slug   string
	UserID int64
}

type UnfavoriteArticleCommand struct {
	Slug   string
	UserID int64
}

type GetArticleQuery struct {
	Slug       string
	ObserverID *int64
}

type GetArticlesQuery struct {
	Tag        *string
	Author     *string
	Favorited  *string
	Limit      int
	Offset     int
	ObserverID *int64
}

type GetFeedQuery struct {
	UserID int64
	Limit  int
	Offset int
}

// --- Cell Ports ---

// Service defines the inbound port for article and tag related use cases.
type Service interface {
	CreateArticle(ctx context.Context, cmd CreateArticleCommand) (*Article, error)
	GetArticle(ctx context.Context, query GetArticleQuery) (*Article, error)
	GetArticles(ctx context.Context, query GetArticlesQuery) ([]*Article, int, error)
	GetFeed(ctx context.Context, query GetFeedQuery) ([]*Article, int, error)
	UpdateArticle(ctx context.Context, cmd UpdateArticleCommand) (*Article, error)
	DeleteArticle(ctx context.Context, cmd DeleteArticleCommand) error
	FavoriteArticle(ctx context.Context, cmd FavoriteArticleCommand) (*Article, error)
	UnfavoriteArticle(ctx context.Context, cmd UnfavoriteArticleCommand) (*Article, error)

	GetTags(ctx context.Context) ([]Tag, error)
}

// Repository defines the outbound port for article and tag data persistence.
type Repository interface {
	Create(ctx context.Context, article *Article, authorID int64) error
	GetBySlug(ctx context.Context, slug string, observerID *int64) (*Article, error)
	GetByTitle(ctx context.Context, title string, observerID *int64) (*Article, error)
	GetArticles(ctx context.Context, params GetArticlesQuery) ([]*Article, int, error)
	GetFeed(ctx context.Context, userID int64, limit, offset int) ([]*Article, int, error)
	Update(ctx context.Context, article *Article) error
	Delete(ctx context.Context, id int64) error
	Favorite(ctx context.Context, articleID, userID int64) error
	Unfavorite(ctx context.Context, articleID, userID int64) error

	FindAllTags(ctx context.Context) ([]Tag, error)
}

// UserProvider defines the interface for information this cell needs from the user cell.
// It matches a subset of user.Service.
type UserProvider interface {
	GetUser(ctx context.Context, id int64) (*user.User, error)
	GetUserByUsername(ctx context.Context, username string) (*user.User, error)
}
