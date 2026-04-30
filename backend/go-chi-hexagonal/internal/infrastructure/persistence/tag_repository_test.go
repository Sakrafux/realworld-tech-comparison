package persistence

import (
	"context"
	"testing"

	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/stretchr/testify/assert"
)

func TestTagRepository_FindAll(t *testing.T) {
	// Setup in-memory SQLite database
	cfg := configuration.DatabaseConfig{
		Type: "sqlite",
	}
	db, err := configuration.NewDatabase(cfg)
	assert.NoError(t, err)
	defer db.Close()

	repo := NewPostgresTagRepository(db)
	ctx := context.Background()

	t.Run("empty tags", func(t *testing.T) {
		tags, err := repo.FindAll(ctx)
		assert.NoError(t, err)
		assert.Empty(t, tags)
	})

	t.Run("with tags", func(t *testing.T) {
		// Seed data
		_, err := db.Exec(`INSERT INTO tag (tag) VALUES ('reactjs'), ('angularjs')`)
		assert.NoError(t, err)

		tags, err := repo.FindAll(ctx)
		assert.NoError(t, err)
		assert.Len(t, tags, 2)

		tagNames := []string{tags[0].Name, tags[1].Name}
		assert.Contains(t, tagNames, "reactjs")
		assert.Contains(t, tagNames, "angularjs")
	})
}
