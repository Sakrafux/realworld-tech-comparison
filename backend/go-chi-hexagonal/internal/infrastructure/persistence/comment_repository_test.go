package persistence

import (
	"context"
	"log/slog"
	"testing"
	"time"

	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/stretchr/testify/assert"
)

func TestCommentRepository(t *testing.T) {
	logger := httplog.NewLogger("test", httplog.Options{
		LogLevel: slog.LevelInfo,
		Concise:  true,
	})
	cfg := configuration.DatabaseConfig{Type: "sqlite"}
	db, err := configuration.NewDatabase(cfg, logger)
	assert.NoError(t, err)
	defer db.Close()

	repo := NewCommentRepository(db)
	userRepo := NewUserRepository(db)
	articleRepo := NewArticleRepository(db)
	ctx := context.Background()

	// 1. Setup user
	user := &domain.User{Username: "commenter", Email: "c@test.com", Password: "p", Bio: "bio"}
	err = userRepo.Create(ctx, user)
	assert.NoError(t, err)

	// 2. Setup article
	article := &domain.Article{
		Slug:        "test-article",
		Title:       "Test Article",
		Description: "Desc",
		Body:        "Body",
	}
	err = articleRepo.Create(ctx, article, user.ID)
	assert.NoError(t, err)

	t.Run("Create success", func(t *testing.T) {
		comment := &domain.Comment{
			Body:      "Nice article!",
			CreatedAt: time.Now(),
			UpdatedAt: time.Now(),
		}

		err := repo.Create(ctx, comment, article.ID, user.ID)

		assert.NoError(t, err)
		assert.NotZero(t, comment.ID)

		// Verify in DB
		var count int
		err = db.GetContext(ctx, &count, "SELECT COUNT(*) FROM comment WHERE id = $1", comment.ID)
		assert.NoError(t, err)
		assert.Equal(t, 1, count)
	})

	t.Run("FindByArticleID success", func(t *testing.T) {
		comments, err := repo.FindByArticleID(ctx, article.ID, nil)

		assert.NoError(t, err)
		assert.Len(t, comments, 1)
		assert.Equal(t, "Nice article!", comments[0].Body)
		assert.Equal(t, user.Username, comments[0].Author.Username)
	})
}
