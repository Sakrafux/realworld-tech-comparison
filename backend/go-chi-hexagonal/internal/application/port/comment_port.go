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

// CommentService defines the inbound port for comment-related use cases.
type CommentService interface {
	CreateComment(ctx context.Context, cmd CreateCommentCommand) (*domain.Comment, error)
}

// CommentRepository defines the outbound port for comment data persistence.
type CommentRepository interface {
	Create(ctx context.Context, comment *domain.Comment, articleID, authorID int64) error
}
