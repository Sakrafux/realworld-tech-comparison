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

func TestCommentHandler_CreateComment(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockCommentService)
		h := NewCommentHandler(svc)

		comReq := createCommentRequest{}
		comReq.Comment.Body = "Test Comment"

		comment := &domain.Comment{
			ID:     1,
			Body:   "Test Comment",
			Author: domain.Profile{Username: "author"},
		}

		svc.On("CreateComment", mock.Anything, mock.MatchedBy(func(cmd port.CreateCommentCommand) bool {
			return cmd.AuthorID == 1 && cmd.Slug == "test-article" && cmd.Body == "Test Comment"
		})).Return(comment, nil)

		body, _ := json.Marshal(comReq)
		req := httptest.NewRequest("POST", "/api/articles/test-article/comments", bytes.NewBuffer(body))

		// Setup context
		ctx := context.WithValue(req.Context(), userIDKey, int64(1))
		rctx := chi.NewRouteContext()
		rctx.URLParams.Add("slug", "test-article")
		ctx = context.WithValue(ctx, chi.RouteCtxKey, rctx)
		req = req.WithContext(ctx)

		w := httptest.NewRecorder()

		h.CreateComment(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp commentResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, comment.Body, resp.Comment.Body)
		svc.AssertExpectations(t)
	})

	t.Run("validation error", func(t *testing.T) {
		h := NewCommentHandler(nil)
		comReq := createCommentRequest{}
		comReq.Comment.Body = "" // Required

		body, _ := json.Marshal(comReq)
		req := httptest.NewRequest("POST", "/api/articles/test-article/comments", bytes.NewBuffer(body))
		ctx := context.WithValue(req.Context(), userIDKey, int64(1))
		req = req.WithContext(ctx)
		w := httptest.NewRecorder()

		h.CreateComment(w, req)

		assert.Equal(t, http.StatusUnprocessableEntity, w.Code)
	})

	t.Run("unauthorized", func(t *testing.T) {
		h := NewCommentHandler(nil)
		req := httptest.NewRequest("POST", "/api/articles/test-article/comments", nil)
		w := httptest.NewRecorder()

		h.CreateComment(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})
}
