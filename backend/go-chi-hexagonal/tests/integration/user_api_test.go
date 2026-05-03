package integration

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/web"
	"github.com/stretchr/testify/assert"
)

func TestUserAPI_Integration(t *testing.T) {
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

	t.Run("Register failure - validation error", func(t *testing.T) {
		regReq := map[string]interface{}{
			"user": map[string]string{
				"username": "u", // Too short
				"email":    "not-an-email",
				"password": "123", // Too short
			},
		}
		body, _ := json.Marshal(regReq)
		req := httptest.NewRequest("POST", "/api/users", bytes.NewBuffer(body))
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnprocessableEntity, w.Code)
		var resp struct {
			Errors struct {
				Body []string `json:"body"`
			} `json:"errors"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Len(t, resp.Errors.Body, 3)
	})
}

func TestUserCurrentAPI_Integration(t *testing.T) {
	cfg := &configuration.Config{
		Database: configuration.DatabaseConfig{Type: "sqlite"},
		Web:      configuration.WebConfig{CorsAllowedOrigins: []string{"*"}},
		Security: configuration.SecurityConfig{JWTSecret: "test-secret"},
	}
	db, err := configuration.NewDatabase(cfg.Database)
	assert.NoError(t, err)
	defer db.Close()

	router := web.NewApp(cfg, db)

	// 1. Register a user to get a token
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

	var regResp struct {
		User struct {
			Token string `json:"token"`
		} `json:"user"`
	}
	json.NewDecoder(w.Body).Decode(&regResp)
	token := regResp.User.Token

	t.Run("Get current user success", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/user", nil)
		req.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)

		var resp struct {
			User struct {
				Username string  `json:"username"`
				Email    string  `json:"email"`
				Bio      string  `json:"bio"`
				Image    *string `json:"image"`
			} `json:"user"`
		}
		err := json.NewDecoder(w.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.Equal(t, "testuser", resp.User.Username)
		assert.Equal(t, "test@example.com", resp.User.Email)
	})

	t.Run("Update current user success", func(t *testing.T) {
		updateReq := map[string]interface{}{
			"user": map[string]string{
				"bio":   "New bio",
				"image": "http://image.com/img.png",
			},
		}
		body, _ := json.Marshal(updateReq)
		req := httptest.NewRequest("PUT", "/api/user", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)

		var resp struct {
			User struct {
				Username string  `json:"username"`
				Bio      string  `json:"bio"`
				Image    *string `json:"image"`
			} `json:"user"`
		}
		err := json.NewDecoder(w.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.Equal(t, "New bio", resp.User.Bio)
		assert.NotNil(t, resp.User.Image)
		assert.Equal(t, "http://image.com/img.png", *resp.User.Image)
	})

	t.Run("Get current user unauthorized", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/user", nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("Update current user email and username success", func(t *testing.T) {
		updateReq := map[string]interface{}{
			"user": map[string]string{
				"username": "newusername",
				"email":    "new@example.com",
			},
		}
		body, _ := json.Marshal(updateReq)
		req := httptest.NewRequest("PUT", "/api/user", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Token "+token)
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
		assert.Equal(t, "newusername", resp.User.Username)
		assert.Equal(t, "new@example.com", resp.User.Email)
		assert.NotEmpty(t, resp.User.Token)

		// Update token for subsequent tests if any
		token = resp.User.Token
	})
}
