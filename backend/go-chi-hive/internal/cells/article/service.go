package article

import (
	"context"
	"time"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/errors"
)

type service struct {
	repo         Repository
	userProvider UserProvider
}

func NewService(repo Repository, userProvider UserProvider) Service {
	return &service{
		repo:         repo,
		userProvider: userProvider,
	}
}

func (s *service) CreateArticle(ctx context.Context, cmd CreateArticleCommand) (*Article, error) {
	author, err := s.userProvider.GetUser(ctx, cmd.AuthorID)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if author == nil {
		return nil, errors.NewResourceNotFound("User", "id", cmd.AuthorID)
	}

	// Check if article with same title already exists
	existingArticle, err := s.repo.GetByTitle(ctx, cmd.Title, nil)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if existingArticle != nil {
		return nil, errors.NewAlreadyExistsError("Article with this title already exists")
	}

	slug := Slugify(cmd.Title)

	// Check if article with same slug already exists
	existingArticle, err = s.repo.GetBySlug(ctx, slug, nil)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if existingArticle != nil {
		return nil, errors.NewAlreadyExistsError("Article with this slug already exists")
	}

	article := &Article{
		Slug:           slug,
		Title:          cmd.Title,
		Description:    cmd.Description,
		Body:           cmd.Body,
		TagList:        cmd.TagList,
		CreatedAt:      time.Now(),
		UpdatedAt:      time.Now(),
		Favorited:      false,
		FavoritesCount: 0,
		Author: Author{
			Username:  author.Username,
			Bio:       author.Bio,
			Image:     author.Image,
			Following: false,
		},
	}

	if err := s.repo.Create(ctx, article, author.ID); err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return article, nil
}

func (s *service) GetArticle(ctx context.Context, query GetArticleQuery) (*Article, error) {
	article, err := s.repo.GetBySlug(ctx, query.Slug, query.ObserverID)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, errors.NewResourceNotFound("Article", "slug", query.Slug)
	}

	return article, nil
}

func (s *service) GetArticles(ctx context.Context, query GetArticlesQuery) ([]*Article, int, error) {
	articles, count, err := s.repo.GetArticles(ctx, query)
	if err != nil {
		return nil, 0, errors.NewInternalError(err.Error())
	}
	return articles, count, nil
}

func (s *service) GetFeed(ctx context.Context, query GetFeedQuery) ([]*Article, int, error) {
	articles, count, err := s.repo.GetFeed(ctx, query.UserID, query.Limit, query.Offset)
	if err != nil {
		return nil, 0, errors.NewInternalError(err.Error())
	}
	return articles, count, nil
}

func (s *service) UpdateArticle(ctx context.Context, cmd UpdateArticleCommand) (*Article, error) {
	// Get article and check if it exists
	article, err := s.repo.GetBySlug(ctx, cmd.Slug, &cmd.UserID)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, errors.NewResourceNotFound("Article", "slug", cmd.Slug)
	}

	// Check if user is author
	author, err := s.userProvider.GetUserByUsername(ctx, article.Author.Username)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if author == nil || author.ID != cmd.UserID {
		return nil, errors.NewForbiddenError("you are not the author of this article")
	}

	// Define duplicate check function
	checkDuplicate := func(title, slug string) error {
		// Check title
		existing, err := s.repo.GetByTitle(ctx, title, nil)
		if err != nil {
			return errors.NewInternalError(err.Error())
		}
		if existing != nil && existing.ID != article.ID {
			return errors.NewAlreadyExistsError("Article with this title already exists")
		}

		// Check slug
		existing, err = s.repo.GetBySlug(ctx, slug, nil)
		if err != nil {
			return errors.NewInternalError(err.Error())
		}
		if existing != nil && existing.ID != article.ID {
			return errors.NewAlreadyExistsError("Article with this slug already exists")
		}
		return nil
	}

	if err := article.Update(cmd.Title, cmd.Description, cmd.Body, checkDuplicate); err != nil {
		return nil, err
	}

	if err := s.repo.Update(ctx, article); err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return article, nil
}

func (s *service) DeleteArticle(ctx context.Context, cmd DeleteArticleCommand) error {
	article, err := s.repo.GetBySlug(ctx, cmd.Slug, &cmd.UserID)
	if err != nil {
		return errors.NewInternalError(err.Error())
	}
	if article == nil {
		return errors.NewResourceNotFound("Article", "slug", cmd.Slug)
	}

	author, err := s.userProvider.GetUserByUsername(ctx, article.Author.Username)
	if err != nil {
		return errors.NewInternalError(err.Error())
	}
	if author == nil || author.ID != cmd.UserID {
		return errors.NewForbiddenError("you are not the author of this article")
	}

	if err := s.repo.Delete(ctx, article.ID); err != nil {
		return errors.NewInternalError(err.Error())
	}

	return nil
}

func (s *service) FavoriteArticle(ctx context.Context, cmd FavoriteArticleCommand) (*Article, error) {
	article, err := s.repo.GetBySlug(ctx, cmd.Slug, &cmd.UserID)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, errors.NewResourceNotFound("Article", "slug", cmd.Slug)
	}

	if err := s.repo.Favorite(ctx, article.ID, cmd.UserID); err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return s.repo.GetBySlug(ctx, cmd.Slug, &cmd.UserID)
}

func (s *service) UnfavoriteArticle(ctx context.Context, cmd UnfavoriteArticleCommand) (*Article, error) {
	article, err := s.repo.GetBySlug(ctx, cmd.Slug, &cmd.UserID)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, errors.NewResourceNotFound("Article", "slug", cmd.Slug)
	}

	if err := s.repo.Unfavorite(ctx, article.ID, cmd.UserID); err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return s.repo.GetBySlug(ctx, cmd.Slug, &cmd.UserID)
}

func (s *service) GetTags(ctx context.Context) ([]Tag, error) {
	return s.repo.FindAllTags(ctx)
}
