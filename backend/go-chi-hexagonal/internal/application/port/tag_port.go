package port

import (
	"context"

	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type TagService interface {
	GetTags(ctx context.Context) ([]domain.Tag, error)
}

type TagRepository interface {
	FindAll(ctx context.Context) ([]domain.Tag, error)
}
