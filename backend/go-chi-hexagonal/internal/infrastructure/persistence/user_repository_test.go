package persistence

import (
	"context"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/stretchr/testify/assert"
)

func TestUserRepository(t *testing.T) {
	cfg := configuration.DatabaseConfig{Type: "sqlite"}
	db, err := configuration.NewDatabase(cfg)
	assert.NoError(t, err)
	defer db.Close()

	repo := NewUserRepository(db)
	ctx := context.Background()

	t.Run("Create success", func(t *testing.T) {
		user := &domain.User{
			Username: "testuser",
			Email:    "test@example.com",
			Password: "hashedpassword",
			Bio:      "my bio",
		}

		err := repo.Create(ctx, user)
		assert.NoError(t, err)
		assert.NotZero(t, user.ID)

		// Verify it was created
		found, err := repo.FindByID(ctx, user.ID)
		assert.NoError(t, err)
		assert.Equal(t, user.Username, found.Username)
		assert.Equal(t, user.Email, found.Email)
	})

	t.Run("FindByEmail", func(t *testing.T) {
		user := &domain.User{Username: "user2", Email: "user2@test.com", Password: "p"}
		repo.Create(ctx, user)

		found, err := repo.FindByEmail(ctx, "user2@test.com")
		assert.NoError(t, err)
		assert.NotNil(t, found)
		assert.Equal(t, user.Username, found.Username)

		none, err := repo.FindByEmail(ctx, "nonexistent@test.com")
		assert.NoError(t, err)
		assert.Nil(t, none)
	})

	t.Run("FindByUsername", func(t *testing.T) {
		found, err := repo.FindByUsername(ctx, "testuser")
		assert.NoError(t, err)
		assert.NotNil(t, found)
		assert.Equal(t, "test@example.com", found.Email)
	})

	t.Run("Update", func(t *testing.T) {
		user, _ := repo.FindByEmail(ctx, "test@example.com")
		newBio := "Updated bio"
		user.Bio = newBio
		user.Image = nil

		err := repo.Update(ctx, user)
		assert.NoError(t, err)

		updated, _ := repo.FindByID(ctx, user.ID)
		assert.Equal(t, newBio, updated.Bio)
		assert.Nil(t, updated.Image)
	})
}
