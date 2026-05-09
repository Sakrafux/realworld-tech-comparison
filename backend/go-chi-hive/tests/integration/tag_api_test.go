package integration

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/hive"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/config"
	"github.com/stretchr/testify/assert"
)

func TestTagsAPI_Integration(t *testing.T) {
	// 1. SETUP: Use the real bootstrapping logic
	cfg := &config.Config{
		Database: config.DatabaseConfig{Type: "sqlite"},
		Web:      config.WebConfig{CorsAllowedOrigins: []string{"*"}},
		Security: config.SecurityConfig{JWTSecret: "test-secret"},
	}
	logger := httplog.NewLogger("test")

	app, _ := hive.NewApp(cfg, logger)

	// 2. SEED: Insert real data into the DB
	_, err := app.DB.Exec(`INSERT INTO tag (tag) VALUES ('golang'), ('hexagonal'), ('realworld')`)
	assert.NoError(t, err)

	// 3. EXECUTE: Call the actual Router
	req := httptest.NewRequest("GET", "/api/tags", nil)
	w := httptest.NewRecorder()
	app.Router.ServeHTTP(w, req)

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
