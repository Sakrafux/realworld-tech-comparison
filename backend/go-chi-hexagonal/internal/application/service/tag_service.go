package service

import (
	"context"

	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type tagService struct {
	tagRepo port.TagRepository
}

func NewTagService(tagRepo port.TagRepository) port.TagService {
	return &tagService{
		tagRepo: tagRepo,
	}
}

func (s *tagService) GetTags(ctx context.Context) ([]domain.Tag, error) {
	return s.tagRepo.FindAll(ctx)
}
