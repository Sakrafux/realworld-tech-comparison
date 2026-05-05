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

func TestCommentService_CreateComment(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		comRepo := new(testmocks.MockCommentRepository)
		artRepo := new(testmocks.MockArticleRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewCommentService(comRepo, artRepo, userRepo)

		article := &domain.Article{ID: 1, Slug: "test-slug"}
		user := &domain.User{ID: 10, Username: "commenter"}

		artRepo.On("GetBySlug", ctx, "test-slug", mock.Anything).Return(article, nil)
		userRepo.On("FindByID", ctx, int64(10)).Return(user, nil)
		comRepo.On("Create", ctx, mock.AnythingOfType("*domain.Comment"), int64(1), int64(10)).Return(nil)

		comment, err := svc.CreateComment(ctx, port.CreateCommentCommand{
			Slug:     "test-slug",
			AuthorID: 10,
			Body:     "Great work!",
		})

		assert.NoError(t, err)
		assert.Equal(t, "Great work!", comment.Body)
		assert.Equal(t, "commenter", comment.Author.Username)
		comRepo.AssertExpectations(t)
	})

	t.Run("article not found", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewCommentService(nil, artRepo, nil)

		artRepo.On("GetBySlug", ctx, "invalid", mock.Anything).Return(nil, nil)

		_, err := svc.CreateComment(ctx, port.CreateCommentCommand{
			Slug:     "invalid",
			AuthorID: 10,
			Body:     "Body",
		})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
	})
}

func TestCommentService_GetComments(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		comRepo := new(testmocks.MockCommentRepository)
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewCommentService(comRepo, artRepo, nil)

		article := &domain.Article{ID: 1, Slug: "test-slug"}
		comments := []domain.Comment{
			{ID: 1, Body: "Comment 1"},
			{ID: 2, Body: "Comment 2"},
		}

		artRepo.On("GetBySlug", ctx, "test-slug", (*int64)(nil)).Return(article, nil)
		comRepo.On("FindByArticleID", ctx, int64(1), (*int64)(nil)).Return(comments, nil)

		result, err := svc.GetComments(ctx, port.GetCommentsQuery{
			Slug:       "test-slug",
			ObserverID: nil,
		})

		assert.NoError(t, err)
		assert.Len(t, result, 2)
		assert.Equal(t, "Comment 1", result[0].Body)
	})

	t.Run("article not found", func(t *testing.T) {
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewCommentService(nil, artRepo, nil)

		artRepo.On("GetBySlug", ctx, "invalid", (*int64)(nil)).Return(nil, nil)

		_, err := svc.GetComments(ctx, port.GetCommentsQuery{
			Slug: "invalid",
		})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
	})
}

func TestCommentService_DeleteComment(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		comRepo := new(testmocks.MockCommentRepository)
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewCommentService(comRepo, artRepo, nil)

		article := &domain.Article{ID: 1, Slug: "test-slug"}
		comment := &domain.Comment{ID: 10}

		artRepo.On("GetBySlug", ctx, "test-slug", (*int64)(nil)).Return(article, nil)
		comRepo.On("GetByID", ctx, int64(10)).Return(comment, int64(1), int64(100), nil)
		comRepo.On("Delete", ctx, int64(10)).Return(nil)

		err := svc.DeleteComment(ctx, port.DeleteCommentCommand{
			Slug:      "test-slug",
			CommentID: 10,
			UserID:    100,
		})

		assert.NoError(t, err)
		artRepo.AssertExpectations(t)
		comRepo.AssertExpectations(t)
	})

	t.Run("unauthorized", func(t *testing.T) {
		comRepo := new(testmocks.MockCommentRepository)
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewCommentService(comRepo, artRepo, nil)

		article := &domain.Article{ID: 1, Slug: "test-slug"}
		comment := &domain.Comment{ID: 10}

		artRepo.On("GetBySlug", ctx, "test-slug", (*int64)(nil)).Return(article, nil)
		comRepo.On("GetByID", ctx, int64(10)).Return(comment, int64(1), int64(200), nil)

		err := svc.DeleteComment(ctx, port.DeleteCommentCommand{
			Slug:      "test-slug",
			CommentID: 10,
			UserID:    100, // Different user
		})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeUnauthorized, err.(domain.AppError).Type)
	})

	t.Run("article mismatch", func(t *testing.T) {
		comRepo := new(testmocks.MockCommentRepository)
		artRepo := new(testmocks.MockArticleRepository)
		svc := NewCommentService(comRepo, artRepo, nil)

		article := &domain.Article{ID: 1, Slug: "test-slug"}
		comment := &domain.Comment{ID: 10}

		artRepo.On("GetBySlug", ctx, "test-slug", (*int64)(nil)).Return(article, nil)
		comRepo.On("GetByID", ctx, int64(10)).Return(comment, int64(2), int64(100), nil) // Different article ID

		err := svc.DeleteComment(ctx, port.DeleteCommentCommand{
			Slug:      "test-slug",
			CommentID: 10,
			UserID:    100,
		})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
	})
}
