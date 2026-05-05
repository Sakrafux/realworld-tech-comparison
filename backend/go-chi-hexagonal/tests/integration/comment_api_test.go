package integration

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strconv"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/web"
	"github.com/stretchr/testify/assert"
)

func TestCommentAPI_Integration(t *testing.T) {
	cfg := &configuration.Config{
		Database: configuration.DatabaseConfig{Type: "sqlite"},
		Web:      configuration.WebConfig{CorsAllowedOrigins: []string{"*"}},
		Security: configuration.SecurityConfig{JWTSecret: "test-secret"},
	}
	db, err := configuration.NewDatabase(cfg.Database)
	assert.NoError(t, err)
	defer db.Close()

	router := web.NewApp(cfg, db)

	// 1. Register a user
	regReq := map[string]any{
		"user": map[string]string{
			"username": "commenter",
			"email":    "commenter@example.com",
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

	// 2. Create an article
	artReq := map[string]any{
		"article": map[string]any{
			"title":       "Commentable Article",
			"description": "Desc",
			"body":        "Body",
		},
	}
	bodyArt, _ := json.Marshal(artReq)
	reqArt := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(bodyArt))
	reqArt.Header.Set("Authorization", "Token "+token)
	wArt := httptest.NewRecorder()
	router.ServeHTTP(wArt, reqArt)
	assert.Equal(t, http.StatusCreated, wArt.Code)

	t.Run("Create comment success", func(t *testing.T) {
		comReq := map[string]any{
			"comment": map[string]any{
				"body": "This is a great article!",
			},
		}
		body, _ := json.Marshal(comReq)
		req := httptest.NewRequest("POST", "/api/articles/commentable-article/comments", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)

		var resp struct {
			Comment struct {
				ID     int64  `json:"id"`
				Body   string `json:"body"`
				Author struct {
					Username string `json:"username"`
				} `json:"author"`
			} `json:"comment"`
		}
		err := json.NewDecoder(w.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.Equal(t, "This is a great article!", resp.Comment.Body)
		assert.Equal(t, "commenter", resp.Comment.Author.Username)
		assert.True(t, resp.Comment.ID > 0)
	})

	t.Run("Get comments success", func(t *testing.T) {
		// Article 'commentable-article' already has one comment from the previous test
		req := httptest.NewRequest("GET", "/api/articles/commentable-article/comments", nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp struct {
			Comments []struct {
				Body string `json:"body"`
			} `json:"comments"`
		}
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Len(t, resp.Comments, 1)
		assert.Equal(t, "This is a great article!", resp.Comments[0].Body)
	})

	t.Run("Create comment article not found", func(t *testing.T) {
		comReq := map[string]any{
			"comment": map[string]any{
				"body": "Body",
			},
		}
		body, _ := json.Marshal(comReq)
		req := httptest.NewRequest("POST", "/api/articles/non-existent/comments", bytes.NewBuffer(body))
		req.Header.Set("Authorization", "Token "+token)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusNotFound, w.Code)
	})

	t.Run("Create comment unauthorized", func(t *testing.T) {
		comReq := map[string]any{
			"comment": map[string]any{
				"body": "Body",
			},
		}
		body, _ := json.Marshal(comReq)
		req := httptest.NewRequest("POST", "/api/articles/commentable-article/comments", bytes.NewBuffer(body))
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("Delete comment success", func(t *testing.T) {
		// First create a comment to delete
		comReq := map[string]any{
			"comment": map[string]any{
				"body": "To be deleted",
			},
		}
		body, _ := json.Marshal(comReq)
		reqCreate := httptest.NewRequest("POST", "/api/articles/commentable-article/comments", bytes.NewBuffer(body))
		reqCreate.Header.Set("Authorization", "Token "+token)
		wCreate := httptest.NewRecorder()
		router.ServeHTTP(wCreate, reqCreate)
		assert.Equal(t, http.StatusOK, wCreate.Code)

		var respCreate struct {
			Comment struct {
				ID int64 `json:"id"`
			} `json:"comment"`
		}
		json.NewDecoder(wCreate.Body).Decode(&respCreate)
		commentID := respCreate.Comment.ID

		// Now delete it
		reqDelete := httptest.NewRequest("DELETE", "/api/articles/commentable-article/comments/"+strconv.FormatInt(commentID, 10), nil)
		reqDelete.Header.Set("Authorization", "Token "+token)
		wDelete := httptest.NewRecorder()
		router.ServeHTTP(wDelete, reqDelete)

		assert.Equal(t, http.StatusOK, wDelete.Code)

		// Verify it's gone
		reqGet := httptest.NewRequest("GET", "/api/articles/commentable-article/comments", nil)
		wGet := httptest.NewRecorder()
		router.ServeHTTP(wGet, reqGet)
		var respGet struct {
			Comments []struct {
				ID int64 `json:"id"`
			} `json:"comments"`
		}
		json.NewDecoder(wGet.Body).Decode(&respGet)
		for _, c := range respGet.Comments {
			assert.NotEqual(t, commentID, c.ID)
		}
	})
}
