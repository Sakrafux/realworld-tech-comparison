package port

import (
	"context"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

// CreateCommentCommand represents the data needed to create a new comment.
type CreateCommentCommand struct {
	Slug     string
	AuthorID int64
	Body     string
}

// DeleteCommentCommand represents the data needed to delete a comment.
type DeleteCommentCommand struct {
	Slug      string
	CommentID int64
	UserID    int64
}

type GetCommentsQuery struct {
	Slug       string
	ObserverID *int64
}

// CommentService defines the inbound port for comment-related use cases.
type CommentService interface {
	CreateComment(ctx context.Context, cmd CreateCommentCommand) (*domain.Comment, error)
	GetComments(ctx context.Context, query GetCommentsQuery) ([]domain.Comment, error)
	DeleteComment(ctx context.Context, cmd DeleteCommentCommand) error
}

// CommentRepository defines the outbound port for comment data persistence.
type CommentRepository interface {
	Create(ctx context.Context, comment *domain.Comment, articleID, authorID int64) error
	FindByArticleID(ctx context.Context, articleID int64, observerID *int64) ([]domain.Comment, error)
	GetByID(ctx context.Context, id int64) (*domain.Comment, int64, int64, error)
	Delete(ctx context.Context, id int64) error
}
