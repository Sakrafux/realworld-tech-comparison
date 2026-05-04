package persistence

import (
	"context"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/stretchr/testify/assert"
)

func TestArticleRepository(t *testing.T) {
	cfg := configuration.DatabaseConfig{Type: "sqlite"}
	db, err := configuration.NewDatabase(cfg)
	assert.NoError(t, err)
	defer db.Close()

	repo := NewArticleRepository(db)
	userRepo := NewUserRepository(db) // Re-using userRepo implementation for setup
	ctx := context.Background()

	// Setup author
	author := &domain.User{Username: "author", Email: "author@test.com", Password: "p", Bio: "bio"}
	err = userRepo.Create(ctx, author)
	assert.NoError(t, err)

	t.Run("Create success with tags", func(t *testing.T) {
		article := &domain.Article{
			Slug:        "test-article",
			Title:       "Test Article",
			Description: "Desc",
			Body:        "Body",
			TagList:     []string{"tag1", "tag2"},
		}

		err := repo.Create(ctx, article, author.ID)
		assert.NoError(t, err)
		assert.NotZero(t, article.ID)

		// Verify it was created
		found, err := repo.GetBySlug(ctx, article.Slug)
		assert.NoError(t, err)
		assert.Equal(t, article.Title, found.Title)
		assert.ElementsMatch(t, article.TagList, found.TagList)
		assert.Equal(t, author.Username, found.Author.Username)
	})

	t.Run("GetByTitle", func(t *testing.T) {
		found, err := repo.GetByTitle(ctx, "Test Article")
		assert.NoError(t, err)
		assert.NotNil(t, found)
		assert.Equal(t, "test-article", found.Slug)

		none, err := repo.GetByTitle(ctx, "Non-existent")
		assert.NoError(t, err)
		assert.Nil(t, none)
	})

	t.Run("GetBySlug non-existent", func(t *testing.T) {
		none, err := repo.GetBySlug(ctx, "none")
		assert.NoError(t, err)
		assert.Nil(t, none)
	})
}
