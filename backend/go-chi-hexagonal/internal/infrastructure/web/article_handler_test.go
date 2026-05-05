package web

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/go-chi/chi/v5"
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

func TestArticleHandler_GetArticle(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockArticleService)
		h := NewArticleHandler(svc)

		article := &domain.Article{Slug: "test", Title: "Test"}
		svc.On("GetArticle", mock.Anything, port.GetArticleQuery{Slug: "test", ObserverID: nil}).Return(article, nil)

		req := httptest.NewRequest("GET", "/api/articles/test", nil)
		// chi context is usually handled by the router, but here we can mock chi.URLParam by manually adding it to the context if needed,
		// but since we are calling the handler directly, we might need to use chi.NewRouteContext()
		rctx := chi.NewRouteContext()
		rctx.URLParams.Add("slug", "test")
		req = req.WithContext(context.WithValue(req.Context(), chi.RouteCtxKey, rctx))

		w := httptest.NewRecorder()

		h.GetArticle(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp articleResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, "test", resp.Article.Slug)
	})

	t.Run("not found", func(t *testing.T) {
		svc := new(testmocks.MockArticleService)
		h := NewArticleHandler(svc)

		svc.On("GetArticle", mock.Anything, port.GetArticleQuery{Slug: "none", ObserverID: nil}).Return(nil, domain.NewNotFoundError("not found"))

		req := httptest.NewRequest("GET", "/api/articles/none", nil)
		rctx := chi.NewRouteContext()
		rctx.URLParams.Add("slug", "none")
		req = req.WithContext(context.WithValue(req.Context(), chi.RouteCtxKey, rctx))

		w := httptest.NewRecorder()

		h.GetArticle(w, req)

		assert.Equal(t, http.StatusNotFound, w.Code)
	})

	t.Run("success authenticated", func(t *testing.T) {
		svc := new(testmocks.MockArticleService)
		h := NewArticleHandler(svc)

		observerID := int64(1)
		article := &domain.Article{Slug: "test", Title: "Test"}
		svc.On("GetArticle", mock.Anything, port.GetArticleQuery{Slug: "test", ObserverID: &observerID}).Return(article, nil)

		req := httptest.NewRequest("GET", "/api/articles/test", nil)
		ctx := context.WithValue(req.Context(), userIDKey, observerID)
		rctx := chi.NewRouteContext()
		rctx.URLParams.Add("slug", "test")
		req = req.WithContext(context.WithValue(ctx, chi.RouteCtxKey, rctx))

		w := httptest.NewRecorder()

		h.GetArticle(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		svc.AssertExpectations(t)
	})
}

func TestArticleHandler_GetFeed(t *testing.T) {
	t.Run("success with defaults", func(t *testing.T) {
		svc := new(testmocks.MockArticleService)
		h := NewArticleHandler(svc)

		userID := int64(1)
		articles := []*domain.Article{{Slug: "test", Title: "Test"}}
		count := 1
		svc.On("GetFeed", mock.Anything, port.GetFeedQuery{UserID: userID, Limit: 20, Offset: 0}).Return(articles, count, nil)

		req := httptest.NewRequest("GET", "/api/articles/feed", nil)
		ctx := context.WithValue(req.Context(), userIDKey, userID)
		req = req.WithContext(ctx)

		w := httptest.NewRecorder()

		h.GetFeed(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp multipleArticlesResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, count, resp.ArticlesCount)
		assert.Len(t, resp.Articles, 1)
		assert.Equal(t, "test", resp.Articles[0].Slug)
		svc.AssertExpectations(t)
	})

	t.Run("success with query params", func(t *testing.T) {
		svc := new(testmocks.MockArticleService)
		h := NewArticleHandler(svc)

		userID := int64(1)
		articles := []*domain.Article{{Slug: "test", Title: "Test"}}
		count := 1
		svc.On("GetFeed", mock.Anything, port.GetFeedQuery{UserID: userID, Limit: 10, Offset: 5}).Return(articles, count, nil)

		req := httptest.NewRequest("GET", "/api/articles/feed?limit=10&offset=5", nil)
		ctx := context.WithValue(req.Context(), userIDKey, userID)
		req = req.WithContext(ctx)

		w := httptest.NewRecorder()

		h.GetFeed(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		svc.AssertExpectations(t)
	})

	t.Run("unauthorized", func(t *testing.T) {
		h := NewArticleHandler(nil)
		req := httptest.NewRequest("GET", "/api/articles/feed", nil)
		w := httptest.NewRecorder()

		h.GetFeed(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})
}
