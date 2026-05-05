package service

import (
	"context"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/tests/testmocks"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestArticleService_CreateArticle(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewArticleService(artRepo, userRepo)

		author := &domain.User{ID: 1, Username: "author", Bio: "bio"}
		cmd := port.CreateArticleCommand{
			AuthorID:    1,
			Title:       "Test Article",
			Description: "Desc",
			Body:        "Body",
			TagList:     []string{"tag1"},
		}

		userRepo.On("FindByID", ctx, int64(1)).Return(author, nil)
		artRepo.On("GetByTitle", ctx, cmd.Title, (*int64)(nil)).Return(nil, nil)
		artRepo.On("GetBySlug", ctx, "test-article", (*int64)(nil)).Return(nil, nil)
		artRepo.On("Create", ctx, mock.AnythingOfType("*domain.Article"), int64(1)).Return(nil)

		article, err := svc.CreateArticle(ctx, cmd)

		assert.NoError(t, err)
		assert.Equal(t, "test-article", article.Slug)
		assert.Equal(t, cmd.Title, article.Title)
		assert.Equal(t, author.Username, article.Author.Username)
		artRepo.AssertExpectations(t)
		userRepo.AssertExpectations(t)
	})

	t.Run("author not found", func(t *testing.T) {
		userRepo := new(testmocks.MockUserRepository)
		svc := NewArticleService(nil, userRepo)
		userRepo.On("FindByID", ctx, int64(1)).Return(nil, nil)

		_, err := svc.CreateArticle(ctx, port.CreateArticleCommand{AuthorID: 1})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
	})

	t.Run("duplicate title", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewArticleService(artRepo, userRepo)

		author := &domain.User{ID: 1}
		cmd := port.CreateArticleCommand{AuthorID: 1, Title: "Existing"}
		userRepo.On("FindByID", ctx, int64(1)).Return(author, nil)
		artRepo.On("GetByTitle", ctx, "Existing", (*int64)(nil)).Return(&domain.Article{}, nil)

		_, err := svc.CreateArticle(ctx, cmd)

		assert.Error(t, err)
		assert.Equal(t, domain.TypeAlreadyExists, err.(domain.AppError).Type)
	})
}

func TestArticleService_GetArticle(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewArticleService(artRepo, nil)
		expected := &domain.Article{Slug: "test"}
		artRepo.On("GetBySlug", ctx, "test", (*int64)(nil)).Return(expected, nil)

		article, err := svc.GetArticle(ctx, port.GetArticleQuery{Slug: "test"})

		assert.NoError(t, err)
		assert.Equal(t, expected, article)
	})

	t.Run("not found", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewArticleService(artRepo, nil)
		artRepo.On("GetBySlug", ctx, "none", (*int64)(nil)).Return(nil, nil)

		_, err := svc.GetArticle(ctx, port.GetArticleQuery{Slug: "none"})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
	})
}

func TestArticleService_GetArticles(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewArticleService(artRepo, nil)
		expectedArticles := []*domain.Article{{Slug: "test1"}, {Slug: "test2"}}
		expectedCount := 2

		query := port.GetArticlesQuery{Limit: 20, Offset: 0}
		artRepo.On("GetArticles", ctx, query).Return(expectedArticles, expectedCount, nil)

		articles, count, err := svc.GetArticles(ctx, query)

		assert.NoError(t, err)
		assert.Equal(t, expectedCount, count)
		assert.Equal(t, expectedArticles, articles)
		artRepo.AssertExpectations(t)
	})
}

func TestArticleService_GetFeed(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewArticleService(artRepo, nil)
		expectedArticles := []*domain.Article{{Slug: "test1"}, {Slug: "test2"}}
		expectedCount := 2

		artRepo.On("GetFeed", ctx, int64(1), 20, 0).Return(expectedArticles, expectedCount, nil)

		articles, count, err := svc.GetFeed(ctx, port.GetFeedQuery{UserID: 1, Limit: 20, Offset: 0})

		assert.NoError(t, err)
		assert.Equal(t, expectedCount, count)
		assert.Equal(t, expectedArticles, articles)
		artRepo.AssertExpectations(t)
	})
}

func TestArticleService_UpdateArticle(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewArticleService(artRepo, userRepo)

		oldArticle := &domain.Article{
			ID:    1,
			Slug:  "old-title",
			Title: "Old Title",
			Author: domain.Profile{
				Username: "author",
			},
		}
		author := &domain.User{ID: 1, Username: "author"}

		newTitle := "New Title"
		cmd := port.UpdateArticleCommand{
			Slug:   "old-title",
			UserID: 1,
			Title:  &newTitle,
		}

		artRepo.On("GetBySlug", ctx, "old-title", mock.Anything).Return(oldArticle, nil)
		userRepo.On("FindByUsername", ctx, "author").Return(author, nil)
		artRepo.On("GetByTitle", ctx, "New Title", (*int64)(nil)).Return(nil, nil)
		artRepo.On("GetBySlug", ctx, "new-title", (*int64)(nil)).Return(nil, nil)
		artRepo.On("Update", ctx, mock.AnythingOfType("*domain.Article")).Return(nil)

		article, err := svc.UpdateArticle(ctx, cmd)

		assert.NoError(t, err)
		assert.Equal(t, "new-title", article.Slug)
		assert.Equal(t, "New Title", article.Title)
		artRepo.AssertExpectations(t)
	})

	t.Run("forbidden", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewArticleService(artRepo, userRepo)

		article := &domain.Article{
			Slug: "test",
			Author: domain.Profile{
				Username: "author",
			},
		}
		user := &domain.User{ID: 1, Username: "author"}

		artRepo.On("GetBySlug", ctx, "test", mock.Anything).Return(article, nil)
		userRepo.On("FindByUsername", ctx, "author").Return(user, nil)

		_, err := svc.UpdateArticle(ctx, port.UpdateArticleCommand{
			Slug:   "test",
			UserID: 2, // Different user
		})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeForbidden, err.(domain.AppError).Type)
	})
}

func TestArticleService_DeleteArticle(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewArticleService(artRepo, userRepo)

		article := &domain.Article{
			ID:   1,
			Slug: "test",
			Author: domain.Profile{
				Username: "author",
			},
		}
		author := &domain.User{ID: 1, Username: "author"}

		artRepo.On("GetBySlug", ctx, "test", mock.Anything).Return(article, nil)
		userRepo.On("FindByUsername", ctx, "author").Return(author, nil)
		artRepo.On("Delete", ctx, int64(1)).Return(nil)

		err := svc.DeleteArticle(ctx, port.DeleteArticleCommand{
			Slug:   "test",
			UserID: 1,
		})

		assert.NoError(t, err)
		artRepo.AssertExpectations(t)
	})

	t.Run("forbidden", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewArticleService(artRepo, userRepo)

		article := &domain.Article{
			ID:   1,
			Slug: "test",
			Author: domain.Profile{
				Username: "author",
			},
		}
		user := &domain.User{ID: 1, Username: "author"}

		artRepo.On("GetBySlug", ctx, "test", mock.Anything).Return(article, nil)
		userRepo.On("FindByUsername", ctx, "author").Return(user, nil)

		err := svc.DeleteArticle(ctx, port.DeleteArticleCommand{
			Slug:   "test",
			UserID: 2, // Different user
		})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeForbidden, err.(domain.AppError).Type)
	})
}

func TestArticleService_FavoriteArticle(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewArticleService(artRepo, nil)

		article := &domain.Article{ID: 1, Slug: "test"}
		artRepo.On("GetBySlug", ctx, "test", mock.Anything).Return(article, nil).Once()
		artRepo.On("Favorite", ctx, int64(1), int64(10)).Return(nil)

		favoritedArticle := &domain.Article{ID: 1, Slug: "test", Favorited: true, FavoritesCount: 1}
		artRepo.On("GetBySlug", ctx, "test", mock.Anything).Return(favoritedArticle, nil).Once()

		result, err := svc.FavoriteArticle(ctx, port.FavoriteArticleCommand{
			Slug:   "test",
			UserID: 10,
		})

		assert.NoError(t, err)
		assert.True(t, result.Favorited)
		assert.Equal(t, 1, result.FavoritesCount)
		artRepo.AssertExpectations(t)
	})
}
