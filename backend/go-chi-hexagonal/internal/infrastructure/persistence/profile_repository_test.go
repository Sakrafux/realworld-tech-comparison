package persistence

import (
	"context"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/stretchr/testify/assert"
)

func TestProfileRepository(t *testing.T) {
	cfg := configuration.DatabaseConfig{Type: "sqlite"}
	db, err := configuration.NewDatabase(cfg)
	assert.NoError(t, err)
	defer db.Close()

	repo := NewPostgresProfileRepository(db)
	userRepo := NewPostgresUserRepository(db)
	ctx := context.Background()

	// Setup users
	user1 := &domain.User{Username: "user1", Email: "user1@test.com", Password: "p", Bio: "b1"}
	userRepo.Create(ctx, user1)
	user2 := &domain.User{Username: "user2", Email: "user2@test.com", Password: "p", Bio: "b2"}
	userRepo.Create(ctx, user2)

	t.Run("GetProfileByUsername anonymous", func(t *testing.T) {
		profile, err := repo.GetProfileByUsername(ctx, "user1", nil)
		assert.NoError(t, err)
		assert.NotNil(t, profile)
		assert.Equal(t, "user1", profile.Username)
		assert.False(t, profile.Following)
	})

	t.Run("Follow and GetProfileByUsername authenticated", func(t *testing.T) {
		err := repo.Follow(ctx, user1.ID, user2.ID)
		assert.NoError(t, err)

		profile, err := repo.GetProfileByUsername(ctx, "user2", &user1.ID)
		assert.NoError(t, err)
		assert.True(t, profile.Following)

		// Get user1 profile from user2's perspective (should be false)
		profile1, _ := repo.GetProfileByUsername(ctx, "user1", &user2.ID)
		assert.False(t, profile1.Following)
	})

	t.Run("Unfollow", func(t *testing.T) {
		err := repo.Unfollow(ctx, user1.ID, user2.ID)
		assert.NoError(t, err)

		profile, err := repo.GetProfileByUsername(ctx, "user2", &user1.ID)
		assert.NoError(t, err)
		assert.False(t, profile.Following)
	})

	t.Run("GetProfileByUsername not found", func(t *testing.T) {
		profile, err := repo.GetProfileByUsername(ctx, "nonexistent", nil)
		assert.NoError(t, err)
		assert.Nil(t, profile)
	})
}
