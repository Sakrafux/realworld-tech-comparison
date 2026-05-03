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

func TestProfileAPI_Integration(t *testing.T) {
	cfg := &configuration.Config{
		Database: configuration.DatabaseConfig{Type: "sqlite"},
		Web:      configuration.WebConfig{CorsAllowedOrigins: []string{"*"}},
		Security: configuration.SecurityConfig{JWTSecret: "test-secret"},
	}
	db, err := configuration.NewDatabase(cfg.Database)
	assert.NoError(t, err)
	defer db.Close()

	router := web.NewApp(cfg, db)

	// 1. Register two users
	token1 := registerUser(t, router, "user1", "user1@example.com")
	registerUser(t, router, "user2", "user2@example.com")

	t.Run("Get profile anonymous", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/profiles/user1", nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp struct {
			Profile struct {
				Username  string `json:"username"`
				Following bool   `json:"following"`
			} `json:"profile"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, "user1", resp.Profile.Username)
		assert.False(t, resp.Profile.Following)
	})

	t.Run("Follow user success", func(t *testing.T) {
		req := httptest.NewRequest("POST", "/api/profiles/user2/follow", nil)
		req.Header.Set("Authorization", "Token "+token1)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp struct {
			Profile struct {
				Username  string `json:"username"`
				Following bool   `json:"following"`
			} `json:"profile"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, "user2", resp.Profile.Username)
		assert.True(t, resp.Profile.Following)
	})

	t.Run("Get profile authenticated - following", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/profiles/user2", nil)
		req.Header.Set("Authorization", "Token "+token1)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp struct {
			Profile struct {
				Username  string `json:"username"`
				Following bool   `json:"following"`
			} `json:"profile"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.True(t, resp.Profile.Following)
	})

	t.Run("Unfollow user success", func(t *testing.T) {
		req := httptest.NewRequest("DELETE", "/api/profiles/user2/follow", nil)
		req.Header.Set("Authorization", "Token "+token1)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp struct {
			Profile struct {
				Username  string `json:"username"`
				Following bool   `json:"following"`
			} `json:"profile"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.False(t, resp.Profile.Following)
	})

	t.Run("Get profile not found", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/profiles/nonexistent", nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusNotFound, w.Code)
	})
}

func registerUser(t *testing.T, router http.Handler, username, email string) string {
	regReq := map[string]interface{}{
		"user": map[string]string{
			"username": username,
			"email":    email,
			"password": "password123",
		},
	}
	body, _ := json.Marshal(regReq)
	req := httptest.NewRequest("POST", "/api/users", bytes.NewBuffer(body))
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	var resp struct {
		User struct {
			Token string `json:"token"`
		} `json:"user"`
	}
	json.NewDecoder(w.Body).Decode(&resp)
	return resp.User.Token
}
