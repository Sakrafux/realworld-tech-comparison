package integration

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/go-chi/httplog/v2"
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
	logger := httplog.NewLogger("test")
	db, err := configuration.NewDatabase(cfg.Database, logger)
	assert.NoError(t, err)
	defer db.Close()

	router := web.NewApp(cfg, db, logger)

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

	t.Run("Get articles success", func(t *testing.T) {
		req := httptest.NewRequest("GET", "/api/articles?tag=dragons", nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp struct {
			Articles []struct {
				Slug string `json:"slug"`
			} `json:"articles"`
			ArticlesCount int `json:"articlesCount"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, 1, resp.ArticlesCount)
		assert.Len(t, resp.Articles, 1)
		assert.Equal(t, "how-to-train-your-dragon", resp.Articles[0].Slug)
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

	t.Run("Update article success", func(t *testing.T) {
		updateReq := map[string]any{
			"article": map[string]any{
				"title":       "How to train your dragon 2",
				"description": "Ever wonder how? Now you know.",
			},
		}
		body, _ := json.Marshal(updateReq)
		req := httptest.NewRequest("PUT", "/api/articles/how-to-train-your-dragon", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)

		var resp struct {
			Article struct {
				Slug        string `json:"slug"`
				Title       string `json:"title"`
				Description string `json:"description"`
				Body        string `json:"body"`
			} `json:"article"`
		}
		err := json.NewDecoder(w.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.Equal(t, "how-to-train-your-dragon-2", resp.Article.Slug)
		assert.Equal(t, "How to train your dragon 2", resp.Article.Title)
		assert.Equal(t, "Ever wonder how? Now you know.", resp.Article.Description)
		assert.Equal(t, "You have to believe", resp.Article.Body) // Body should remain unchanged
	})

	t.Run("Update article forbidden", func(t *testing.T) {
		// Register another user
		regReq2 := map[string]any{
			"user": map[string]string{
				"username": "otheruser",
				"email":    "other@example.com",
				"password": "password123",
			},
		}
		body2, _ := json.Marshal(regReq2)
		req2 := httptest.NewRequest("POST", "/api/users", bytes.NewBuffer(body2))
		w2 := httptest.NewRecorder()
		router.ServeHTTP(w2, req2)
		assert.Equal(t, http.StatusCreated, w2.Code)

		var regResp2 struct {
			User struct {
				Token string `json:"token"`
			} `json:"user"`
		}
		json.NewDecoder(w2.Body).Decode(&regResp2)
		otherToken := regResp2.User.Token

		updateReq := map[string]any{
			"article": map[string]any{
				"title": "Hack the dragon",
			},
		}
		body, _ := json.Marshal(updateReq)
		req := httptest.NewRequest("PUT", "/api/articles/how-to-train-your-dragon-2", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Token "+otherToken)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusForbidden, w.Code)
	})

	t.Run("Delete article success", func(t *testing.T) {
		req := httptest.NewRequest("DELETE", "/api/articles/how-to-train-your-dragon-2", nil)
		req.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)

		// Verify it's gone
		reqGet := httptest.NewRequest("GET", "/api/articles/how-to-train-your-dragon-2", nil)
		wGet := httptest.NewRecorder()
		router.ServeHTTP(wGet, reqGet)
		assert.Equal(t, http.StatusNotFound, wGet.Code)
	})

	t.Run("Favorite article success", func(t *testing.T) {
		// 1. Create an article first
		artReq := map[string]any{
			"article": map[string]any{
				"title":       "Favorite Me",
				"description": "Desc",
				"body":        "Body",
			},
		}
		body, _ := json.Marshal(artReq)
		reqCreate := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(body))
		reqCreate.Header.Set("Authorization", "Token "+token)
		wCreate := httptest.NewRecorder()
		router.ServeHTTP(wCreate, reqCreate)
		assert.Equal(t, http.StatusCreated, wCreate.Code)

		// 2. Favorite it
		reqFav := httptest.NewRequest("POST", "/api/articles/favorite-me/favorite", nil)
		reqFav.Header.Set("Authorization", "Token "+token)
		wFav := httptest.NewRecorder()
		router.ServeHTTP(wFav, reqFav)

		assert.Equal(t, http.StatusOK, wFav.Code)

		var resp struct {
			Article struct {
				Favorited      bool `json:"favorited"`
				FavoritesCount int  `json:"favoritesCount"`
			} `json:"article"`
		}
		err := json.NewDecoder(wFav.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.True(t, resp.Article.Favorited)
		assert.Equal(t, 1, resp.Article.FavoritesCount)
	})
}
