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
		artRepo.On("GetByTitle", ctx, cmd.Title).Return(nil, nil)
		artRepo.On("GetBySlug", ctx, "test-article").Return(nil, nil)
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
		artRepo.On("GetByTitle", ctx, "Existing").Return(&domain.Article{}, nil)

		_, err := svc.CreateArticle(ctx, cmd)

		assert.Error(t, err)
		assert.Equal(t, domain.TypeAlreadyExists, err.(domain.AppError).Type)
	})
}
