package persistence

import (
	"context"
	"log/slog"
	"testing"
	"time"

	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/stretchr/testify/assert"
)

func TestArticleRepository(t *testing.T) {
	logger := httplog.NewLogger("test", httplog.Options{
		LogLevel: slog.LevelInfo,
		Concise:  true,
	})
	cfg := configuration.DatabaseConfig{Type: "sqlite"}
	db, err := configuration.NewDatabase(cfg, logger)
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
		found, err := repo.GetBySlug(ctx, article.Slug, nil)
		assert.NoError(t, err)
		assert.Equal(t, article.Title, found.Title)
		assert.ElementsMatch(t, article.TagList, found.TagList)
		assert.Equal(t, author.Username, found.Author.Username)
	})

	t.Run("GetByTitle", func(t *testing.T) {
		found, err := repo.GetByTitle(ctx, "Test Article", nil)
		assert.NoError(t, err)
		assert.NotNil(t, found)
		assert.Equal(t, "test-article", found.Slug)

		none, err := repo.GetByTitle(ctx, "Non-existent", nil)
		assert.NoError(t, err)
		assert.Nil(t, none)
	})

	t.Run("GetBySlug non-existent", func(t *testing.T) {
		none, err := repo.GetBySlug(ctx, "none", nil)
		assert.NoError(t, err)
		assert.Nil(t, none)
	})

	t.Run("Following status", func(t *testing.T) {
		follower := &domain.User{Username: "follower", Email: "follower@test.com", Password: "p", Bio: "bio"}
		userRepo.Create(ctx, follower)
		profileRepo := NewProfileRepository(db)
		profileRepo.Follow(ctx, follower.ID, author.ID)

		found, err := repo.GetBySlug(ctx, "test-article", &follower.ID)
		assert.NoError(t, err)
		assert.True(t, found.Author.Following)

		unauth, _ := repo.GetBySlug(ctx, "test-article", nil)
		assert.False(t, unauth.Author.Following)
	})

	t.Run("GetArticles with tag filter", func(t *testing.T) {
		tag := "tag1"
		articles, count, err := repo.GetArticles(ctx, port.GetArticlesQuery{Tag: &tag, Limit: 10, Offset: 0})
		assert.NoError(t, err)
		assert.Equal(t, 1, count)
		assert.Len(t, articles, 1)
		assert.Equal(t, "test-article", articles[0].Slug)
	})

	t.Run("GetFeed", func(t *testing.T) {
		follower := &domain.User{Username: "feed-follower", Email: "ff@test.com", Password: "p", Bio: "bio"}
		userRepo.Create(ctx, follower)
		profileRepo := NewProfileRepository(db)
		profileRepo.Follow(ctx, follower.ID, author.ID)

		// Create a second article from author to test sorting and pagination
		article2 := &domain.Article{
			Slug:        "test-article-2",
			Title:       "Test Article 2",
			Description: "Desc",
			Body:        "Body",
			CreatedAt:   time.Now().Add(time.Minute),
			UpdatedAt:   time.Now().Add(time.Minute),
		}
		err := repo.Create(ctx, article2, author.ID)
		assert.NoError(t, err)

		articles, count, err := repo.GetFeed(ctx, follower.ID, 1, 0)
		assert.NoError(t, err)
		// Now author has 2 articles
		assert.Equal(t, 2, count)
		assert.Len(t, articles, 1)
		// article2 is newer, should be returned first
		assert.Equal(t, "test-article-2", articles[0].Slug)

		// Test offset
		articles, _, err = repo.GetFeed(ctx, follower.ID, 1, 1)
		assert.NoError(t, err)
		assert.Len(t, articles, 1)
		assert.Equal(t, "test-article", articles[0].Slug)

		// User that follows no one
		loner := &domain.User{Username: "loner", Email: "loner@test.com", Password: "p", Bio: "bio"}
		userRepo.Create(ctx, loner)

		articles, count, err = repo.GetFeed(ctx, loner.ID, 10, 0)
		assert.NoError(t, err)
		assert.Equal(t, 0, count)
		assert.Len(t, articles, 0)
	})
}
