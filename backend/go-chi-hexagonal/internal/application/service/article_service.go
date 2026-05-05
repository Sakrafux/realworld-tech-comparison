package service

import (
	"context"
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
	existingArticle, err := s.articleRepo.GetByTitle(ctx, cmd.Title, nil)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if existingArticle != nil {
		return nil, domain.NewAlreadyExistsError("Article with this title already exists")
	}

	slug := domain.Slugify(cmd.Title)

	// Check if article with same slug already exists
	existingArticle, err = s.articleRepo.GetBySlug(ctx, slug, nil)
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

func (s *articleService) GetArticle(ctx context.Context, query port.GetArticleQuery) (*domain.Article, error) {
	article, err := s.articleRepo.GetBySlug(ctx, query.Slug, query.ObserverID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, domain.NewResourceNotFound("Article", "slug", query.Slug)
	}

	return article, nil
}

func (s *articleService) UpdateArticle(ctx context.Context, cmd port.UpdateArticleCommand) (*domain.Article, error) {
	// Get article and check if it exists
	article, err := s.articleRepo.GetBySlug(ctx, cmd.Slug, &cmd.UserID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, domain.NewResourceNotFound("Article", "slug", cmd.Slug)
	}

	// Check if user is author
	author, err := s.userRepo.FindByUsername(ctx, article.Author.Username)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if author == nil || author.ID != cmd.UserID {
		return nil, domain.NewForbiddenError("you are not the author of this article")
	}

	// Define duplicate check function
	checkDuplicate := func(title, slug string) error {
		// Check title
		existing, err := s.articleRepo.GetByTitle(ctx, title, nil)
		if err != nil {
			return domain.NewInternalError(err.Error())
		}
		if existing != nil && existing.ID != article.ID {
			return domain.NewAlreadyExistsError("Article with this title already exists")
		}

		// Check slug
		existing, err = s.articleRepo.GetBySlug(ctx, slug, nil)
		if err != nil {
			return domain.NewInternalError(err.Error())
		}
		if existing != nil && existing.ID != article.ID {
			return domain.NewAlreadyExistsError("Article with this slug already exists")
		}
		return nil
	}

	if err := article.Update(cmd.Title, cmd.Description, cmd.Body, checkDuplicate); err != nil {
		return nil, err
	}

	if err := s.articleRepo.Update(ctx, article); err != nil {
		return nil, domain.NewInternalError(err.Error())
	}

	return article, nil
}

func (s *articleService) DeleteArticle(ctx context.Context, slug string, userID int64) error {
	article, err := s.articleRepo.GetBySlug(ctx, slug, &userID)
	if err != nil {
		return domain.NewInternalError(err.Error())
	}
	if article == nil {
		return domain.NewResourceNotFound("Article", "slug", slug)
	}

	author, err := s.userRepo.FindByUsername(ctx, article.Author.Username)
	if err != nil {
		return domain.NewInternalError(err.Error())
	}
	if author == nil || author.ID != userID {
		return domain.NewForbiddenError("you are not the author of this article")
	}

	if err := s.articleRepo.Delete(ctx, article.ID); err != nil {
		return domain.NewInternalError(err.Error())
	}

	return nil
}

func (s *articleService) FavoriteArticle(ctx context.Context, slug string, userID int64) (*domain.Article, error) {
	article, err := s.articleRepo.GetBySlug(ctx, slug, &userID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, domain.NewResourceNotFound("Article", "slug", slug)
	}

	if err := s.articleRepo.Favorite(ctx, article.ID, userID); err != nil {
		return nil, domain.NewInternalError(err.Error())
	}

	return s.articleRepo.GetBySlug(ctx, slug, &userID)
}

func (s *articleService) UnfavoriteArticle(ctx context.Context, slug string, userID int64) (*domain.Article, error) {
	article, err := s.articleRepo.GetBySlug(ctx, slug, &userID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, domain.NewResourceNotFound("Article", "slug", slug)
	}

	if err := s.articleRepo.Unfavorite(ctx, article.ID, userID); err != nil {
		return nil, domain.NewInternalError(err.Error())
	}

	return s.articleRepo.GetBySlug(ctx, slug, &userID)
}
