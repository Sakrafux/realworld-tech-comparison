package comment

import (
	"context"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/cells/article"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/cells/user"
)

// --- Commands & Queries ---

type CreateCommentCommand struct {
	Slug     string
	AuthorID int64
	Body     string
}

type DeleteCommentCommand struct {
	Slug      string
	CommentID int64
	UserID    int64
}

type GetCommentsQuery struct {
	Slug       string
	ObserverID *int64
}

// --- Cell Ports ---

// Service defines the inbound port for comment-related use cases.
type Service interface {
	CreateComment(ctx context.Context, cmd CreateCommentCommand) (*Comment, error)
	GetComments(ctx context.Context, query GetCommentsQuery) ([]Comment, error)
	DeleteComment(ctx context.Context, cmd DeleteCommentCommand) error
}

// Repository defines the outbound port for comment data persistence.
type Repository interface {
	Create(ctx context.Context, comment *Comment, articleID, authorID int64) error
	FindByArticleID(ctx context.Context, articleID int64, observerID *int64) ([]Comment, error)
	GetByID(ctx context.Context, id int64) (*Comment, int64, int64, error)
	Delete(ctx context.Context, id int64) error
}

// UserProvider defines the interface for information this cell needs from the user cell.
type UserProvider interface {
	GetUser(ctx context.Context, id int64) (*user.User, error)
}

// ArticleProvider defines the interface for information this cell needs from the article cell.
type ArticleProvider interface {
	GetArticle(ctx context.Context, query article.GetArticleQuery) (*article.Article, error)
}
