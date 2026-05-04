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

func TestArticleAPI_Integration(t *testing.T) {
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
	regReq := map[string]any{
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

	t.Run("Create article success", func(t *testing.T) {
		artReq := map[string]any{
			"article": map[string]any{
				"title":       "How to train your dragon",
				"description": "Ever wonder how?",
				"body":        "You have to believe",
				"tagList":     []string{"reactjs", "angularjs", "dragons"},
			},
		}
		body, _ := json.Marshal(artReq)
		req := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusCreated, w.Code)

		var resp struct {
			Article struct {
				Slug        string   `json:"slug"`
				Title       string   `json:"title"`
				Description string   `json:"description"`
				Body        string   `json:"body"`
				TagList     []string `json:"tagList"`
				Author      struct {
					Username string `json:"username"`
				} `json:"author"`
			} `json:"article"`
		}
		err := json.NewDecoder(w.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.Equal(t, "how-to-train-your-dragon", resp.Article.Slug)
		assert.Equal(t, "How to train your dragon", resp.Article.Title)
		assert.ElementsMatch(t, []string{"reactjs", "angularjs", "dragons"}, resp.Article.TagList)
		assert.Equal(t, "testuser", resp.Article.Author.Username)
	})

	t.Run("Create article unauthorized", func(t *testing.T) {
		artReq := map[string]any{
			"article": map[string]any{
				"title":       "Title",
				"description": "Desc",
				"body":        "Body",
			},
		}
		body, _ := json.Marshal(artReq)
		req := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(body))
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("Create article validation failure", func(t *testing.T) {
		artReq := map[string]any{
			"article": map[string]any{
				"title":       "", // Blank
				"description": string(make([]byte, 256)),
				"body":        "", // Blank
				"tagList":     []string{"this-tag-is-way-too-long-for-the-limit"},
			},
		}
		body, _ := json.Marshal(artReq)
		req := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnprocessableEntity, w.Code)
		var resp struct {
			Errors struct {
				Body []string `json:"body"`
			} `json:"errors"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Len(t, resp.Errors.Body, 4)
	})

	t.Run("Create article failure - duplicate title", func(t *testing.T) {
		artReq := map[string]any{
			"article": map[string]any{
				"title":       "Duplicate Title",
				"description": "Desc",
				"body":        "Body",
			},
		}
		body, _ := json.Marshal(artReq)

		// First creation
		req1 := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(body))
		req1.Header.Set("Authorization", "Token "+token)
		router.ServeHTTP(httptest.NewRecorder(), req1)

		// Second creation with same title
		req2 := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(body))
		req2.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req2)

		assert.Equal(t, http.StatusUnprocessableEntity, w.Code)
		var resp struct {
			Errors struct {
				Body []string `json:"body"`
			} `json:"errors"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Contains(t, resp.Errors.Body[0], "already exists")
	})

	t.Run("Get article success", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/articles/how-to-train-your-dragon", nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp struct {
			Article struct {
				Slug  string `json:"slug"`
				Title string `json:"title"`
			} `json:"article"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, "how-to-train-your-dragon", resp.Article.Slug)
	})

	t.Run("Get article not found", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/articles/non-existent", nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusNotFound, w.Code)
	})
}
