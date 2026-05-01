package integration

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/web"
	"github.com/stretchr/testify/assert"
)

func TestTagsAPI_Integration(t *testing.T) {
	// 1. SETUP: Use the real bootstrapping logic
	cfg := &configuration.Config{
		Database: configuration.DatabaseConfig{Type: "sqlite"},
		Web:      configuration.WebConfig{CorsAllowedOrigins: []string{"*"}},
		Security: configuration.SecurityConfig{JWTSecret: "test-secret"},
	}
	db, err := configuration.NewDatabase(cfg.Database)
	assert.NoError(t, err)
	defer db.Close()

	router := web.NewApp(cfg, db)

	// 2. SEED: Insert real data into the DB
	_, err = db.Exec(`INSERT INTO tag (tag) VALUES ('golang'), ('hexagonal'), ('realworld')`)
	assert.NoError(t, err)

	// 3. EXECUTE: Call the actual Router
	req := httptest.NewRequest("GET", "/api/tags", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	// 4. ASSERT: Verify the full integration
	assert.Equal(t, http.StatusOK, w.Code)
	assert.Equal(t, "application/json", w.Header().Get("Content-Type"))

	var resp struct {
		Tags []string `json:"tags"`
	}
	err = json.NewDecoder(w.Body).Decode(&resp)
	assert.NoError(t, err)

	assert.Len(t, resp.Tags, 3)
	assert.Contains(t, resp.Tags, "golang")
	assert.Contains(t, resp.Tags, "hexagonal")
	assert.Contains(t, resp.Tags, "realworld")
}
