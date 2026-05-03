package web

import (
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

func TestProfileHandler_GetProfile(t *testing.T) {
	t.Run("success anonymous", func(t *testing.T) {
		svc := new(testmocks.MockProfileService)
		h := NewProfileHandler(svc)

		profile := &domain.Profile{Username: "testuser", Bio: "bio", Following: false}
		svc.On("GetProfile", mock.Anything, port.GetProfileQuery{Username: "testuser", ObserverID: nil}).Return(profile, nil)

		req := httptest.NewRequest("GET", "/api/profiles/testuser", nil)
		chiCtx := chi.NewRouteContext()
		chiCtx.URLParams.Add("username", "testuser")
		req = req.WithContext(context.WithValue(req.Context(), chi.RouteCtxKey, chiCtx))

		w := httptest.NewRecorder()

		h.GetProfile(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp profileResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, "testuser", resp.Profile.Username)
		assert.False(t, resp.Profile.Following)
		svc.AssertExpectations(t)
	})

	t.Run("success authenticated", func(t *testing.T) {
		svc := new(testmocks.MockProfileService)
		h := NewProfileHandler(svc)

		profile := &domain.Profile{Username: "testuser", Following: true}
		observerID := int64(1)
		svc.On("GetProfile", mock.Anything, port.GetProfileQuery{Username: "testuser", ObserverID: &observerID}).Return(profile, nil)

		req := httptest.NewRequest("GET", "/api/profiles/testuser", nil)
		chiCtx := chi.NewRouteContext()
		chiCtx.URLParams.Add("username", "testuser")
		ctx := context.WithValue(req.Context(), chi.RouteCtxKey, chiCtx)
		ctx = context.WithValue(ctx, userIDKey, observerID)
		req = req.WithContext(ctx)

		w := httptest.NewRecorder()

		h.GetProfile(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp profileResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.True(t, resp.Profile.Following)
		svc.AssertExpectations(t)
	})
}

func TestProfileHandler_Follow(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockProfileService)
		h := NewProfileHandler(svc)

		profile := &domain.Profile{Username: "testuser", Following: true}
		userID := int64(1)
		svc.On("FollowUser", mock.Anything, port.FollowUserCommand{FollowerID: userID, Username: "testuser"}).Return(profile, nil)

		req := httptest.NewRequest("POST", "/api/profiles/testuser/follow", nil)
		chiCtx := chi.NewRouteContext()
		chiCtx.URLParams.Add("username", "testuser")
		ctx := context.WithValue(req.Context(), chi.RouteCtxKey, chiCtx)
		ctx = context.WithValue(ctx, userIDKey, userID)
		req = req.WithContext(ctx)

		w := httptest.NewRecorder()

		h.Follow(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp profileResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.True(t, resp.Profile.Following)
		svc.AssertExpectations(t)
	})

	t.Run("unauthorized", func(t *testing.T) {
		svc := new(testmocks.MockProfileService)
		h := NewProfileHandler(svc)

		req := httptest.NewRequest("POST", "/api/profiles/testuser/follow", nil)
		w := httptest.NewRecorder()

		h.Follow(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})
}

func TestProfileHandler_Unfollow(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockProfileService)
		h := NewProfileHandler(svc)

		profile := &domain.Profile{Username: "testuser", Following: false}
		userID := int64(1)
		svc.On("UnfollowUser", mock.Anything, port.UnfollowUserCommand{FollowerID: userID, Username: "testuser"}).Return(profile, nil)

		req := httptest.NewRequest("DELETE", "/api/profiles/testuser/follow", nil)
		chiCtx := chi.NewRouteContext()
		chiCtx.URLParams.Add("username", "testuser")
		ctx := context.WithValue(req.Context(), chi.RouteCtxKey, chiCtx)
		ctx = context.WithValue(ctx, userIDKey, userID)
		req = req.WithContext(ctx)

		w := httptest.NewRecorder()

		h.Unfollow(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp profileResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.False(t, resp.Profile.Following)
		svc.AssertExpectations(t)
	})
}
