package service

import (
	"context"
	"strings"
	"time"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type articleService struct {
	articleRepo port.ArticleRepository
	userRepo    port.UserRepository
}

func NewArticleService(articleRepo port.ArticleRepository, userRepo port.UserRepository) port.ArticleService {
	return &articleService{
		articleRepo: articleRepo,
		userRepo:    userRepo,
	}
}

func (s *articleService) CreateArticle(ctx context.Context, cmd port.CreateArticleCommand) (*domain.Article, error) {
	author, err := s.userRepo.FindByID(ctx, cmd.AuthorID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if author == nil {
		return nil, domain.NewResourceNotFound("User", "id", cmd.AuthorID)
	}

	// Check if article with same title already exists
	existingArticle, err := s.articleRepo.GetByTitle(ctx, cmd.Title)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if existingArticle != nil {
		return nil, domain.NewAlreadyExistsError("Article with this title already exists")
	}

	slug := slugify(cmd.Title)

	// Check if article with same slug already exists
	existingArticle, err = s.articleRepo.GetBySlug(ctx, slug)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if existingArticle != nil {
		return nil, domain.NewAlreadyExistsError("Article with this slug already exists")
	}

	article := &domain.Article{
		Slug:           slug,
		Title:          cmd.Title,
		Description:    cmd.Description,
		Body:           cmd.Body,
		TagList:        cmd.TagList,
		CreatedAt:      time.Now(),
		UpdatedAt:      time.Now(),
		Favorited:      false,
		FavoritesCount: 0,
		Author: domain.Profile{
			Username:  author.Username,
			Bio:       author.Bio,
			Image:     author.Image,
			Following: false,
		},
	}

	if err := s.articleRepo.Create(ctx, article, author.ID); err != nil {
		return nil, domain.NewInternalError(err.Error())
	}

	return article, nil
}

func slugify(title string) string {
	return strings.ToLower(strings.ReplaceAll(title, " ", "-"))
}
