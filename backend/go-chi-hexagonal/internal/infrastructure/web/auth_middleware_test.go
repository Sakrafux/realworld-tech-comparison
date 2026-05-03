package web

import (
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/tests/testmocks"
	"github.com/stretchr/testify/assert"
)

func TestAuthMiddleware(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		tg := new(testmocks.MockTokenGenerator)
		tg.On("Parse", "valid-token").Return(int64(1), nil)

		middleware := AuthMiddleware(tg)
		handler := middleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			userID, ok := GetUserIDFromContext(r.Context())
			assert.True(t, ok)
			assert.Equal(t, int64(1), userID)
			w.WriteHeader(http.StatusOK)
		}))

		req := httptest.NewRequest("GET", "/", nil)
		req.Header.Set("Authorization", "Token valid-token")
		w := httptest.NewRecorder()

		handler.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		tg.AssertExpectations(t)
	})

	t.Run("missing header", func(t *testing.T) {
		tg := new(testmocks.MockTokenGenerator)
		middleware := AuthMiddleware(tg)
		handler := middleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {}))

		req := httptest.NewRequest("GET", "/", nil)
		w := httptest.NewRecorder()

		handler.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("invalid format", func(t *testing.T) {
		tg := new(testmocks.MockTokenGenerator)
		middleware := AuthMiddleware(tg)
		handler := middleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {}))

		req := httptest.NewRequest("GET", "/", nil)
		req.Header.Set("Authorization", "Bearer token")
		w := httptest.NewRecorder()

		handler.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("invalid token", func(t *testing.T) {
		tg := new(testmocks.MockTokenGenerator)
		tg.On("Parse", "invalid").Return(int64(0), errors.New("invalid"))

		middleware := AuthMiddleware(tg)
		handler := middleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {}))

		req := httptest.NewRequest("GET", "/", nil)
		req.Header.Set("Authorization", "Token invalid")
		w := httptest.NewRecorder()

		handler.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})
}

func TestOptionalAuthMiddleware(t *testing.T) {
	t.Run("success with token", func(t *testing.T) {
		tg := new(testmocks.MockTokenGenerator)
		tg.On("Parse", "valid").Return(int64(1), nil)

		middleware := OptionalAuthMiddleware(tg)
		handler := middleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			userID, ok := GetUserIDFromContext(r.Context())
			assert.True(t, ok)
			assert.Equal(t, int64(1), userID)
			w.WriteHeader(http.StatusOK)
		}))

		req := httptest.NewRequest("GET", "/", nil)
		req.Header.Set("Authorization", "Token valid")
		w := httptest.NewRecorder()

		handler.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
	})

	t.Run("success without token", func(t *testing.T) {
		tg := new(testmocks.MockTokenGenerator)

		middleware := OptionalAuthMiddleware(tg)
		handler := middleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			_, ok := GetUserIDFromContext(r.Context())
			assert.False(t, ok)
			w.WriteHeader(http.StatusOK)
		}))

		req := httptest.NewRequest("GET", "/", nil)
		w := httptest.NewRecorder()

		handler.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
	})

	t.Run("success with invalid token", func(t *testing.T) {
		tg := new(testmocks.MockTokenGenerator)
		tg.On("Parse", "invalid").Return(int64(0), errors.New("invalid"))

		middleware := OptionalAuthMiddleware(tg)
		handler := middleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			_, ok := GetUserIDFromContext(r.Context())
			assert.False(t, ok)
			w.WriteHeader(http.StatusOK)
		}))

		req := httptest.NewRequest("GET", "/", nil)
		req.Header.Set("Authorization", "Token invalid")
		w := httptest.NewRecorder()

		handler.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
	})
}
