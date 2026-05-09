package integration

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/hive"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/config"
	"github.com/stretchr/testify/assert"
)

func TestProfileAPI_Integration(t *testing.T) {
	cfg := &config.Config{
		Database: config.DatabaseConfig{Type: "sqlite"},
		Web:      config.WebConfig{CorsAllowedOrigins: []string{"*"}},
		Security: config.SecurityConfig{JWTSecret: "test-secret"},
	}
	logger := httplog.NewLogger("test")

	app, _ := hive.NewApp(cfg, logger)

	// 1. Register two users
	token1 := registerUser(t, app.Router, "user1", "user1@example.com")
	registerUser(t, app.Router, "user2", "user2@example.com")

	t.Run("Get profile anonymous", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/profiles/user1", nil)
		w := httptest.NewRecorder()
		app.Router.ServeHTTP(w, req)

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
		app.Router.ServeHTTP(w, req)

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
		app.Router.ServeHTTP(w, req)

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
		app.Router.ServeHTTP(w, req)

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
		app.Router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusNotFound, w.Code)
	})
}

func registerUser(t *testing.T, router http.Handler, username, email string) string {
	regReq := map[string]any{
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
