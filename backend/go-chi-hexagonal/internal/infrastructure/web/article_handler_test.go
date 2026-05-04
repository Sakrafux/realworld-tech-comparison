package web

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/tests/testmocks"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestArticleHandler_CreateArticle(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockArticleService)
		h := NewArticleHandler(svc)

		artReq := createArticleRequest{}
		artReq.Article.Title = "Test Article"
		artReq.Article.Description = "Desc"
		artReq.Article.Body = "Body"
		artReq.Article.TagList = []string{"tag1"}

		article := &domain.Article{
			Slug:   "test-article",
			Title:  artReq.Article.Title,
			Author: domain.Profile{Username: "author"},
		}

		svc.On("CreateArticle", mock.Anything, mock.MatchedBy(func(cmd port.CreateArticleCommand) bool {
			return cmd.AuthorID == 1 && cmd.Title == artReq.Article.Title
		})).Return(article, nil)

		body, _ := json.Marshal(artReq)
		req := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(body))
		ctx := context.WithValue(req.Context(), userIDKey, int64(1))
		req = req.WithContext(ctx)
		w := httptest.NewRecorder()

		h.CreateArticle(w, req)

		assert.Equal(t, http.StatusCreated, w.Code)
		var resp articleResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, article.Slug, resp.Article.Slug)
		svc.AssertExpectations(t)
	})

	t.Run("unauthorized", func(t *testing.T) {
		h := NewArticleHandler(nil)
		req := httptest.NewRequest("POST", "/api/articles", nil)
		w := httptest.NewRecorder()

		h.CreateArticle(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("validation error", func(t *testing.T) {
		h := NewArticleHandler(nil)
		artReq := createArticleRequest{}
		artReq.Article.Title = "" // Required

		body, _ := json.Marshal(artReq)
		req := httptest.NewRequest("POST", "/api/articles", bytes.NewBuffer(body))
		ctx := context.WithValue(req.Context(), userIDKey, int64(1))
		req = req.WithContext(ctx)
		w := httptest.NewRecorder()

		h.CreateArticle(w, req)

		assert.Equal(t, http.StatusUnprocessableEntity, w.Code)
	})
}
