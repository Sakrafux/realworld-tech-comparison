package integration

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/service"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/persistence"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/security"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/web"
	"github.com/stretchr/testify/assert"
)

func TestTagsAPI_Integration(t *testing.T) {
	// 1. SETUP: Real dependencies with in-memory SQLite
	dbCfg := configuration.DatabaseConfig{
		Type: "sqlite",
	}
	db, err := configuration.NewDatabase(dbCfg)
	assert.NoError(t, err)
	defer db.Close()

	webCfg := configuration.WebConfig{
		CorsAllowedOrigins: []string{"*"},
	}

	passwordHasher := security.NewBcryptHasher()
	tokenGenerator := security.NewJWTGenerator("test-secret")
	userRepo := persistence.NewPostgresUserRepository(db)
	userService := service.NewUserService(userRepo, passwordHasher)
	userHandler := web.NewUserHandler(userService, tokenGenerator)

	tagRepo := persistence.NewPostgresTagRepository(db)
	tagSvc := service.NewTagService(tagRepo)
	tagHandler := web.NewTagHandler(tagSvc)

	router := web.NewRouter(webCfg, tagHandler, userHandler)

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
