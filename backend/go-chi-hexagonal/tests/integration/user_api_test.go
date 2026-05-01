package integration

import (
	"bytes"
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

func TestUserAPI_Integration(t *testing.T) {
	// 1. SETUP
	dbCfg := configuration.DatabaseConfig{Type: "sqlite"}
	db, err := configuration.NewDatabase(dbCfg)
	assert.NoError(t, err)
	defer db.Close()

	webCfg := configuration.WebConfig{CorsAllowedOrigins: []string{"*"}}

	passwordHasher := security.NewBcryptHasher()
	tokenGenerator := security.NewJWTGenerator("test-secret")
	userRepo := persistence.NewPostgresUserRepository(db)
	userService := service.NewUserService(userRepo, passwordHasher)
	userHandler := web.NewUserHandler(userService, tokenGenerator)

	tagRepo := persistence.NewPostgresTagRepository(db)
	tagSvc := service.NewTagService(tagRepo)
	tagHandler := web.NewTagHandler(tagSvc)

	router := web.NewRouter(webCfg, tagHandler, userHandler)

	t.Run("Register success", func(t *testing.T) {
		regReq := map[string]interface{}{
			"user": map[string]string{
				"username": "testuser",
				"email":    "test@example.com",
				"password": "password123",
			},
		}
		body, _ := json.Marshal(regReq)
		req := httptest.NewRequest("POST", "/api/users", bytes.NewBuffer(body))
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusCreated, w.Code)

		var resp struct {
			User struct {
				Username string `json:"username"`
				Email    string `json:"email"`
				Token    string `json:"token"`
			} `json:"user"`
		}
		err := json.NewDecoder(w.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.Equal(t, "testuser", resp.User.Username)
		assert.Equal(t, "test@example.com", resp.User.Email)
		assert.NotEmpty(t, resp.User.Token)
	})

	t.Run("Login success", func(t *testing.T) {
		loginReq := map[string]interface{}{
			"user": map[string]string{
				"email":    "test@example.com",
				"password": "password123",
			},
		}
		body, _ := json.Marshal(loginReq)
		req := httptest.NewRequest("POST", "/api/users/login", bytes.NewBuffer(body))
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)

		var resp struct {
			User struct {
				Username string `json:"username"`
				Email    string `json:"email"`
				Token    string `json:"token"`
			} `json:"user"`
		}
		err := json.NewDecoder(w.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.Equal(t, "testuser", resp.User.Username)
		assert.Equal(t, "test@example.com", resp.User.Email)
		assert.NotEmpty(t, resp.User.Token)
	})

	t.Run("Login failure - wrong password", func(t *testing.T) {
		loginReq := map[string]interface{}{
			"user": map[string]string{
				"email":    "test@example.com",
				"password": "wrongpassword",
			},
		}
		body, _ := json.Marshal(loginReq)
		req := httptest.NewRequest("POST", "/api/users/login", bytes.NewBuffer(body))
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("Register failure - existing email", func(t *testing.T) {
		regReq := map[string]interface{}{
			"user": map[string]string{
				"username": "newuser",
				"email":    "test@example.com",
				"password": "password123",
			},
		}
		body, _ := json.Marshal(regReq)
		req := httptest.NewRequest("POST", "/api/users", bytes.NewBuffer(body))
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnprocessableEntity, w.Code)
	})
}
