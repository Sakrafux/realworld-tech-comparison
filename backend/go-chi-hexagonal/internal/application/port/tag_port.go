package port

import (
	"context"

	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

// TagService defines the inbound port for tag-related use cases.
type TagService interface {
	// GetTags retrieves all existing tags.
	GetTags(ctx context.Context) ([]domain.Tag, error)
}

// TagRepository defines the outbound port for tag data persistence.
type TagRepository interface {
	// FindAll retrieves all tags from the underlying data store.
	FindAll(ctx context.Context) ([]domain.Tag, error)
}
