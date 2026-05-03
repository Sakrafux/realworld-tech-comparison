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

func TestUserHandler_GetCurrentUser(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockUserService)
		tg := new(testmocks.MockTokenGenerator)
		h := NewUserHandler(svc, tg)

		user := &domain.User{ID: 1, Username: "testuser", Email: "test@test.com"}
		svc.On("GetUser", mock.Anything, port.GetUserQuery{ID: 1}).Return(user, nil)
		tg.On("Generate", user).Return("token123", nil)

		req := httptest.NewRequest("GET", "/api/user", nil)
		// Manually set user_id in context
		ctx := context.WithValue(req.Context(), userIDKey, int64(1))
		req = req.WithContext(ctx)
		w := httptest.NewRecorder()

		h.GetCurrentUser(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var resp userResponse
		json.NewDecoder(w.Body).Decode(&resp)
		assert.Equal(t, "testuser", resp.User.Username)
		assert.Equal(t, "token123", resp.User.Token)
		svc.AssertExpectations(t)
		tg.AssertExpectations(t)
	})

	t.Run("unauthorized", func(t *testing.T) {
		svc := new(testmocks.MockUserService)
		tg := new(testmocks.MockTokenGenerator)
		h := NewUserHandler(svc, tg)

		req := httptest.NewRequest("GET", "/api/user", nil)
		w := httptest.NewRecorder()

		h.GetCurrentUser(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})
}

func TestUserHandler_Register(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockUserService)
		tg := new(testmocks.MockTokenGenerator)
		h := NewUserHandler(svc, tg)

		regReq := registrationRequest{}
		regReq.User.Username = "testuser"
		regReq.User.Email = "test@test.com"
		regReq.User.Password = "password123"

		user := &domain.User{ID: 1, Username: "testuser", Email: "test@test.com"}
		svc.On("Register", mock.Anything, mock.MatchedBy(func(cmd port.RegisterCommand) bool {
			return cmd.Username == "testuser"
		})).Return(user, nil)
		tg.On("Generate", user).Return("token123", nil)

		body, _ := json.Marshal(regReq)
		req := httptest.NewRequest("POST", "/api/users", bytes.NewBuffer(body))
		w := httptest.NewRecorder()

		h.Register(w, req)

		assert.Equal(t, http.StatusCreated, w.Code)
		svc.AssertExpectations(t)
		tg.AssertExpectations(t)
	})
}

func TestUserHandler_Login(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockUserService)
		tg := new(testmocks.MockTokenGenerator)
		h := NewUserHandler(svc, tg)

		loginReq := loginRequest{}
		loginReq.User.Email = "test@test.com"
		loginReq.User.Password = "password123"

		user := &domain.User{ID: 1, Username: "testuser", Email: "test@test.com"}
		svc.On("Login", mock.Anything, mock.MatchedBy(func(cmd port.LoginCommand) bool {
			return cmd.Email == "test@test.com"
		})).Return(user, nil)
		tg.On("Generate", user).Return("token123", nil)

		body, _ := json.Marshal(loginReq)
		req := httptest.NewRequest("POST", "/api/users/login", bytes.NewBuffer(body))
		w := httptest.NewRecorder()

		h.Login(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		svc.AssertExpectations(t)
		tg.AssertExpectations(t)
	})
}

func TestUserHandler_UpdateCurrentUser(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		svc := new(testmocks.MockUserService)
		tg := new(testmocks.MockTokenGenerator)
		h := NewUserHandler(svc, tg)

		updateReq := updateUserRequest{}
		bio := "new bio"
		updateReq.User.Bio = &bio

		user := &domain.User{ID: 1, Username: "testuser", Email: "test@test.com", Bio: bio}
		svc.On("UpdateUser", mock.Anything, mock.MatchedBy(func(cmd port.UpdateUserCommand) bool {
			return cmd.ID == 1 && *cmd.Bio == bio
		})).Return(user, nil)
		tg.On("Generate", user).Return("token123", nil)

		body, _ := json.Marshal(updateReq)
		req := httptest.NewRequest("PUT", "/api/user", bytes.NewBuffer(body))
		ctx := context.WithValue(req.Context(), userIDKey, int64(1))
		req = req.WithContext(ctx)
		w := httptest.NewRecorder()

		h.UpdateCurrentUser(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		svc.AssertExpectations(t)
		tg.AssertExpectations(t)
	})
}
